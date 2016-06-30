package de.fssd.model

import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.MCState;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix;
import java.util.stream.StreamSupport

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
            for (x in 1..n)
                rv *= x;
            return rv;
        }

        override fun toString(): String {
            return "Subchain{ size=$size, P0=$P0, timeseries=${varmap.values} }"
        }

        fun uniform(sampleCount: Int) {
            // https://en.wikipedia.org/wiki/Uniformization_(probability_theory)
            var nmax = 1000;
            var maxRateGamma = 0.0;

            var Q = matrix.copy()
            for (i in 0..size - 1) {
                Q.setEntry(i, i, 0.0)
                var qii = 0.0
                for (v in Q.getRow(i))
                    qii += v
                Q.setEntry(i, i, -qii)
                maxRateGamma = Math.max(maxRateGamma, qii)
                assert(0 <= qii && qii < Double.POSITIVE_INFINITY)
            }
            System.out.println("γ: " + maxRateGamma)

            val P = createRealIdentityMatrix(size).add(Q.scalarMultiply(1 / maxRateGamma))

            val powers = ArrayList<RealVector>();
            for (n in 0..nmax) {
                powers.add(P.power(n).preMultiply(P0))
            }

            addVectorToSeries(P0)
            for (t in 1..sampleCount) {
                var pt = ArrayRealVector(size, 0.0);
                for (n in 0..nmax) {
                    val f1 = nfac(n);
                    if (f1 == Double.POSITIVE_INFINITY) // TODO: use bignum instead of a Double
                        break
                    val f2 = Math.pow(maxRateGamma * t, n.toDouble())
                    if (f2 / f1 < Math.ulp(0.0)) { // This term and all after it are below machine epsilon
                        System.out.println("Value below machine ε reached for n=$n, n!=$f1, t=$t")
                        break
                    }
                    pt = pt.add(powers[n].mapMultiply((f2 / f1) * Math.exp(- maxRateGamma * t)));
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

    class MarkovException : RuntimeException { constructor(message: String): super(message) { } }

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

    private var sampleCount = 0;
    private var stateToChain = HashMap<MCState, Subchain>()
    private val chains = ArrayList<Subchain>()

    constructor (tree: FaultTree, f: MCComponentFinder, varIDToStateMap: Map<Int, MCState>) {
        sampleCount = tree.sampleCount

        // manage mapping of varID that was defined by the bdd library and init the initial probability
        val nameIdToVarIdMap = HashMap<String, Int>(); // maps the name of sate in file to varId of state given by BDD
        for (varID in varIDToStateMap.keys) {
            val mcState = varIDToStateMap.get(varID) ?: throw MarkovException("Unknown variable $varID")

            nameIdToVarIdMap.put(mcState.id, varID);
        }

        for (s in f.sets) {
            var chain = Subchain(s.size)
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
            c.uniform(sampleCount)
        }
    }

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    override fun getSamplePointsCount(): Int {
        return sampleCount.toInt();
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    override fun getProbabilitySeries(varID: Int): Stream<Float> {
        val vm = HashMap<Int, McVariable>()
        for (c in chains) {
            for (k in c.varmap.keys) {
                vm[k] = c.varmap[k] ?: throw MarkovException("Unkown variable $k")
            }
        }

        if(!vm.containsKey(varID)) {
            throw MarkovException("Invalid varId");
        }

        val s = (vm[varID]?.timeSeries) ?: throw MarkovException("Invalid varid: $varID")
        val i = Spliterators.spliterator(s, 0)
        val x = StreamSupport.stream(i, false)
        return x
    }

    private fun getVarIDs(): Collection<Int> {
        return chains.fold(ArrayList<Int>(), {S, C -> S.union(C.varmap.keys); S})
    }

    override fun areVariableDependent(varID1: Int, varID2: Int): Boolean {
        return stateToChain.filter({ E ->
            val k = E.value.varmap.keys
            k.contains(varID1) || k.contains(varID2)}).size == 1
    }

    fun equalsToTimeSeries(timeSeries: TimeSeries): Boolean {
        for (varID in getVarIDs()) {
            val thisSeries = getProbabilitySeries(varID.toInt()).collect(Collectors.toList());
            val otherSeries = timeSeries.getProbabilitySeries(varID.toInt()).collect(Collectors.toList());
            if(!thisSeries.equals(otherSeries)) {
                return false;
            }
        }
        return true;
    }

    override fun toString(): String {
        return "Markov{sampleCount=$sampleCount, subchains=$chains}"
    }
}
