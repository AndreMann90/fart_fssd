package de.fssd.model

import de.fssd.dataobjects.FaultTree
import de.fssd.dataobjects.MCState
import org.apache.commons.math3.linear.*

import java.util.*

import org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix
import kotlin.system.measureTimeMillis

/**
 * Continuous Markov implementation using uniformization
 *
 * Computes the timeseries from the mc. For performance efficiency, the mc is split into its components
 */
class Markov : TimeSeries, StateDependencies {

    override val samplePointsCount: Int
    private var sampleTime = 0.0f
    private var stateToChain : MutableMap<MCState, Subchain> = mutableMapOf()
    private val chains : MutableList<Subchain> = mutableListOf()

    val variables: List<McVariable>
        get() {
            val r : MutableList<McVariable> = mutableListOf()
            for (c in chains) {
                for (v in c.varmap.values) {
                    r.add(v)
                }
            }
            return r
        }

    constructor (tree: FaultTree, components: List<Set<MCState>>, varIDToStateMap: Map<Int, MCState>) {
        samplePointsCount = tree.sampleCount
        sampleTime = tree.sampleTime

        // manage mapping of varID that was defined by the bdd library and init the initial probability
        val nameIdToVarIdMap = HashMap<String, Int>() // maps the name of sate in file to varId of state given by BDD
        for (varID in varIDToStateMap.keys) {
            val mcState = varIDToStateMap[varID] ?: throw MarkovException("Unknown variable $varID")

            nameIdToVarIdMap.put(mcState.id, varID)
        }

        for ((i, s) in components.withIndex()) {
            val chain = Subchain(s.size)
            chains.add(chain)

            var orderInQ = 0
            for (mcs in s) {
                stateToChain[mcs] = chain
                val fromId = nameIdToVarIdMap[mcs.id] ?: throw MarkovException("Invalid MC State ID ${mcs.id}")
                chain.varmap[fromId] = McVariable(orderInQ, "chain$i - ${mcs.id}")
                orderInQ++
            }

            for (mcs in s) {
                val fromVarId = nameIdToVarIdMap[mcs.id] ?: throw MarkovException("Invalid MC State ID ${mcs.id}")
                val fromOrderId = chain.varmap[fromVarId]?.orderInQ ?: throw MarkovException("Invalid source variable ID $fromVarId")
                chain.P0.setEntry(fromOrderId, mcs.p0.toDouble())
                var residual = 1.0

                for ((rate, state) in mcs.transitions) {
                    val toVarId = nameIdToVarIdMap[state]
                    val toOrderId = chain.varmap[toVarId]?.orderInQ?.toInt() ?: throw MarkovException("Invalid target variable ID $toVarId")

                    chain.matrix.setEntry(fromOrderId, toOrderId, rate.toDouble())
                    residual -= rate
                }
                if ((residual < 0) || (residual > 1)) {
                    /* Weird things happened */
                    throw MarkovException("Invalid state transition from state: " + mcs.id)
                }

                chain.matrix.setEntry(fromOrderId, fromOrderId, residual)
            }
        }

        uniformization()
    }

    private fun uniformization() {
        val timedelta = measureTimeMillis {
            for (c in chains) {
                c.uniform(samplePointsCount, sampleTime)
            }
        }
        System.err.println("Markov chain uniformization took $timedelta milliseconds")
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
                vm[k] = c.varmap[k] ?: throw MarkovException("Unknown variable $k")
            }
        }

        if(!vm.containsKey(varID)) {
            throw MarkovException("Invalid varId")
        }

        return (vm[varID]?.timeSeries) ?: throw MarkovException("Invalid varid: $varID")
    }

    private fun getVarIDs(): Collection<Int> {
        var r: Set<Int> = mutableSetOf()
        for (chain in chains) {
            r = r.union(chain.varmap.keys)
        }
        return r
    }

    override fun areVariableDependent(varID1: Int, varID2: Int): Boolean {
        for (c in chains) {
            if ((varID1 in c.varmap.keys) && (varID2 in c.varmap.keys))
                return true
        }
        return false
    }

    fun equalsToTimeSeries(timeSeries: TimeSeries): Boolean {
        for (varID in getVarIDs()) {
            val thisSeries = getProbabilitySeries(varID.toInt())
            val otherSeries = timeSeries.getProbabilitySeries(varID.toInt())
            val inequalEl = thisSeries?.asSequence()?.zip(otherSeries!!.asSequence()) {a, b -> Math.abs(a - b)}?.find { el -> el >= 0.0001 }
            if(inequalEl != null) {
                System.err.println("inequal Element with diff: $inequalEl")
                return false
            }
        }
        return true
    }

    override fun toString(): String {
        return "Markov{sampleCount=$samplePointsCount, sampleTime=$sampleTime, subchains=$chains}"
    }
}

internal class MarkovException : RuntimeException { constructor(message: String): super(message) { } }

/**
 * Subchain is a component of mc that is not reachable from the other states that doo not belong to component
 */
internal class Subchain constructor (val size: Int) {
    val matrix = BlockRealMatrix(size, size)
    val P0 : RealVector = ArrayRealVector(size)
    val varmap = HashMap<Int, McVariable>()

    private fun addVectorToSeries(vec: RealVector) {
        for (v in varmap.values) {
            val index = v.orderInQ
            val entry = vec.getEntry(index.toInt())
            v.timeSeries.add(entry.toFloat())
        }
    }

    private fun nfac(n: Int): Double {
        var rv: Double = 1.0
        for (x in 1..n)
            rv *= x
        return rv
    }

    override fun toString(): String {
        return "Subchain{ size=$size, P0=$P0, timeseries=${varmap.values} }"
    }

    fun uniform(sampleCount: Int, sampleTime: Float) {
        // https://en.wikipedia.org/wiki/Uniformization_(probability_theory)
        val nmax = 171 /* Our n won't get any higher because n! == Double.Infinity for n >= 171*/
        var maxRateGamma = 0.0

        val Q = matrix.copy()
        for (i in 0..size - 1) {
            Q.setEntry(i, i, 0.0)
            var qii = 0.0
            for (v in Q.getRow(i))
                qii += v
            Q.setEntry(i, i, -qii)
            maxRateGamma = Math.max(maxRateGamma, qii)
            assert(0 <= qii && qii < Double.POSITIVE_INFINITY)
        }

        val P = createRealIdentityMatrix(size).add(Q.scalarMultiply(1 / maxRateGamma))

        val powers : MutableList<RealVector> = mutableListOf(P0)
        var current = P0
        for (n in 1..nmax) {
            current = P.preMultiply(current)
            powers.add(current)
        }

        addVectorToSeries(P0)
        for (t in 1..sampleCount-1) {
            var pt = ArrayRealVector(size, 0.0)
            for (n in 0..nmax) {
                val f1 = nfac(n)
                if (f1 == Double.POSITIVE_INFINITY)
                    break
                val f2 = Math.pow(maxRateGamma * t * sampleTime, n.toDouble())
                if (f2 / f1 < Math.ulp(0.0)) { // This term and all after it are below machine epsilon
                    break
                }
                pt = pt.add(powers[n].mapMultiply((f2 / f1) * Math.exp(- maxRateGamma * t * sampleTime)))
            }
            for (idx in 0..pt.dimension-1) {
                val v = pt.getEntry(idx)
                assert(v >= 0.0)
                assert(v <= 1.0)
            }
            addVectorToSeries(pt)
        }
    }
}
