package de.fssd.model

/**
 * Created by gbe on 6/29/16.
 */

import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.MCState;
import org.apache.commons.math3.linear.*;

import java.util.*;

import org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix;

/**
 * Continuous Markov implementation using uniformization
 */
class Markov : TimeSeries, StateDependencies {

    private class Subchain constructor (size: Int) {
        val size = size
        val matrix = BlockRealMatrix(size, size)
        val P0 = ArrayRealVector(size)
        val varmap = HashMap<Int, McVariable>()

        private fun addVectorToSeries(vec: RealVector) {
            for (v in varmap.values) {
                val index = v.orderInQ;
                val entry = vec.getEntry(index.toInt());
                v.timeSeries.add(entry.toFloat());
            }
        }

        private fun nfac(n: Int): Double {
            var rv: Double = 1.0;
            for (x in 1..n.toInt())
                rv *= x;
            return rv;
        }

        fun uniform(sampleCount: Int) {
            // https://en.wikipedia.org/wiki/Uniformization_(probability_theory)
            val nmax = 1000;
            var maxRateGamma = 0.0;

            val Q = matrix.copy()
            System.out.println("Transition rate matrix: " + Q)

            for (i in 0..size - 1) {
                Q.setEntry(i, i, 0.0)
                var qii = 0.0
                for (v in Q.getRow(i))
                    qii += v
                Q.setEntry(i, i, -qii)
                maxRateGamma = Math.max(maxRateGamma, qii)
                assert(0 <= qii && qii < Double.POSITIVE_INFINITY)
            }
            System.out.println("Q: " + Q)
            System.out.println("Gamma: " + maxRateGamma)

            val P = createRealIdentityMatrix(size).add(Q.scalarMultiply(1 / maxRateGamma))
            System.out.println(P)

            addVectorToSeries(P0)
            for (t in 1..sampleCount) {
                var pt = ArrayRealVector(size, 0.0);
                for (n in 0..nmax) {
                    val f1 = Math.pow(maxRateGamma * t, n.toDouble())
                    val f2 = nfac(n);
                    if (f2 == Double.POSITIVE_INFINITY)
                        break
                    pt = pt.add(P.power(n).preMultiply(P0).mapMultiply((f1 / f2) * Math.exp(- maxRateGamma * t)));
                }
                for (idx in 0..pt.dimension-1) {
                    val v = pt.getEntry(idx);
                    assert(v >= 0.0);
                    assert(v <= 1.0);
                }
                addVectorToSeries(pt);
            }
        }
    }

    override val samplePointsCount: Int
    private var sampleTime = 0.0f;
    private var stateToChain = HashMap<MCState, Subchain>()
    private val chains = ArrayList<Subchain>()

    class MarkovException : RuntimeException { constructor(message: String): super(message) { } }

    /**
     * Represents the Variable for a MC state
     */
    private class McVariable {
        val timeSeries = java.util.ArrayList<Float>();
        var orderInQ : Int = 0; // ordering in generator matrix Q

        constructor (orderInQ: Int) {
            this.orderInQ = orderInQ;
        }

        override fun toString(): String {
            return timeSeries.toString();
        }
    }

    constructor (tree: FaultTree, f: MCComponentFinder, varIDToStateMap: Map<Int, MCState>) {
        samplePointsCount = tree.sampleCount
        sampleTime = tree.missionTime / (samplePointsCount - 1)

        // manage mapping of varID that was defined by the bdd library and init the initial probability
        val nameIdToVarIdMap = HashMap<String, Int>(); // maps the name of sate in file to varId of state given by BDD
        for (varID in varIDToStateMap.keys) {
            val mcState = varIDToStateMap[varID] ?: throw MarkovException("Unknown variable $varID")

            nameIdToVarIdMap.put(mcState.id, varID);
        }

        // fill the matrix and find the maximum entry of this matrix
        for (s in f.sets) {
            val chain = Subchain(s.size)
            chains.add(chain)

            var orderInQ = 0;
            for (mcs in s) {
                stateToChain[mcs] = chain
                val fromId = nameIdToVarIdMap[mcs.id] ?: throw MarkovException("Invalid MC State ID ${mcs.id}")
                chain.varmap[fromId] = McVariable(orderInQ)
                orderInQ++
            }

            for (mcs in s) {
                val fromVarId = nameIdToVarIdMap[mcs.id] ?: throw MarkovException("Invalid MC State ID ${mcs.id}")
                val fromOrderId = chain.varmap[fromVarId]?.orderInQ ?: throw MarkovException("Invalid source variable ID $fromVarId")
                chain.P0.setEntry(fromOrderId, mcs.p0.toDouble())
                var residual = 1.0

                for (tr in mcs.transitions) {
                    val toVarId = nameIdToVarIdMap[tr.state]
                    val toOrderId = chain.varmap[toVarId]?.orderInQ?.toInt() ?: throw MarkovException("Invalid target variable ID $toVarId")

                    val rate = tr.p
                    chain.matrix.setEntry(fromOrderId, toOrderId, rate.toDouble())
                    residual -= rate
                }
                if ((residual < 0) || (residual > 1)) {
                    /* Weird things happened */
                    throw MarkovException("Invalid state transition from state: " + mcs.id)
                }

                chain.matrix.setEntry(fromOrderId, fromOrderId, residual);
            }
        }

        uniformization();
    }

    private fun uniformization() {
        for (c in chains) {
            c.uniform(samplePointsCount)
        }
    }

    fun getSampleTime(): Float {
        return sampleTime;
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    override fun getProbabilitySeries(varID: Int): List<Float>? {
        val vm = HashMap<Int, McVariable>()
        for (c in chains) {
            for (k in c.varmap.keys) {
                vm[k] = c.varmap[k] ?: throw MarkovException("Unkown variable $k")
            }
        }

        if(!vm.containsKey(varID)) {
            throw MarkovException("Invalid varId");
        }

        return (vm[varID]?.timeSeries) ?: throw MarkovException("Invalid varid: $varID")
    }

    private fun getVarIDs(): Collection<Int> {
        return chains.fold(ArrayList<Int>(), {S, C -> S.union(C.varmap.keys); S})
    }

    override fun areVariableDependent(varID1: Int, varID2: Int): Boolean {
        throw UnsupportedOperationException() //TODO
    }

    fun equalsToTimeSeries(timeSeries: TimeSeries): Boolean {
        for (varID in getVarIDs()) {
            val thisSeries = getProbabilitySeries(varID.toInt());
            val otherSeries = timeSeries.getProbabilitySeries(varID.toInt());
            val equal = thisSeries?.equals(otherSeries) ?: (otherSeries == null)
            if(!equal) {
                return false;
            }
        }
        return true;
    }

    override fun toString(): String {
        return "Markov{sampleCount=$samplePointsCount, sampleTime=$sampleTime, subchains=$chains}"
    }
}
