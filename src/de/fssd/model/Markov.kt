package de.fssd.model

/**
 * Created by gbe on 6/29/16.
 */

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
class Markov : TimeSeries {
    lateinit private var P0 : RealVector
    lateinit private var transitionMatrix: BlockRealMatrix

    private var varmap = HashMap<Int, McVariable>();
    private var sampleCount = 0;
    private var sampleTime = 0.0f;

    public class MarkovException : RuntimeException {
        constructor(message: String): super(message) {
        }
    }

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

    constructor (t: FaultTree, varIDToStateMap: Map<Int, MCState>) { // throws MarkovException {
        sampleCount = t.sampleCount
        sampleTime = t.missionTime / (sampleCount - 1)

        val stateCount = t.chain.size

        val identity = BlockRealMatrix(stateCount, stateCount);

        P0 = ArrayRealVector(stateCount);
        transitionMatrix = BlockRealMatrix(stateCount, stateCount);
        val nameIdToVarIdMap = HashMap<String, Int>(); // maps the name of sate in file to varId of state given by BDD

        // manage mapping of varID that was defined by the bdd library and init the initial probability
        var orderInQ = 0;
        for (varID in varIDToStateMap.keys) {
            val mcState = varIDToStateMap.get(varID) ?: throw MarkovException("Unknown variable ${varID}")

            varmap.put(varID, McVariable(orderInQ))
            nameIdToVarIdMap.put(mcState.id, varID);

            P0.setEntry(orderInQ, mcState.p0.toDouble())
            identity.setEntry(orderInQ, orderInQ, 1.0);
            orderInQ++;
        }

        // fill the matrix and find the maximum entry of this matrix
        for (varID in varIDToStateMap.keys) {
            val fromOrderId = varmap.get(varID)?.orderInQ?.toInt() ?: throw MarkovException("Unknown variable " + varID)
            val mcState = varIDToStateMap.get(varID) ?: throw MarkovException("Unknown variable " + varID);

            var residual = 1.0;
            for (tr in mcState.transitions) {
                val toVarId = nameIdToVarIdMap.get(tr.state)
                val rate = tr.p
                val toOrderId = varmap.get(toVarId)?.orderInQ?.toInt() ?: throw MarkovException("Invalid target variable ID")
                // TODO transpose? (change from <-> to)
                transitionMatrix.setEntry(fromOrderId, toOrderId, rate.toDouble())
                residual -= rate
            }
            if ((residual < 0) || (residual > 1)) {
                /* Weird things happened */
                throw MarkovException("Invalid state transition from state: " + mcState.getId());
            }
            System.out.println("Residual state probability: " + residual);
            transitionMatrix.setEntry(fromOrderId, fromOrderId, residual);
        }

        uniformization();
    }

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

    private fun uniformization() {
        // https://en.wikipedia.org/wiki/Uniformization_(probability_theory)
        var idim = transitionMatrix.getRowDimension();
        var jdim = transitionMatrix.getColumnDimension();
        assert(idim == jdim);
        var nmax = 1000;
        var maxRateGamma = 0.0;

        for (i in 0..idim - 1)
            maxRateGamma = Math.max(maxRateGamma, transitionMatrix.getEntry(i, i));

        assert(maxRateGamma > 0 && maxRateGamma <= 1);

        /* TODO: this could be made simpler: P = I + (1/gamma)Q */
        val P = BlockRealMatrix(idim, jdim);
        for (i in 0..idim - 1) {
            var v = P.getEntry(i, i);
            for (j in 0..jdim - 1) {
                if (i == j) // These will be calculated later
                    continue;
                val pij = transitionMatrix.getEntry(i, j) / maxRateGamma;
                v += pij;
                P.setEntry(i, j, pij);
            }
            P.setEntry(i, i, 1 - v);
        }

        addVectorToSeries(P0);
        for (t in 1..sampleCount.toInt()) {
            var pt = ArrayRealVector(idim, 0.0);
            for (n in 0..nmax) {
                val f1 = Math.pow(maxRateGamma * t, n.toDouble()) / nfac(n);
                pt = pt.add(P.power(n).preMultiply(P0).mapMultiply(f1 * Math.exp(- maxRateGamma * t)));
            }
            for (idx in 0..pt.dimension-1) {
                val v = pt.getEntry(idx);
                assert(v >= 0.0);
                assert(v <= 1.0);
            }
            addVectorToSeries(pt);
        }
    }

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    override public fun getSamplePointsCount(): Int {
        return sampleCount.toInt();
    }

    public fun getSampleTime(): Float {
        return sampleTime;
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    override fun getProbabilitySeries(varID: Int): Stream<Float> {
        if(!varmap.containsKey(varID)) {
            throw MarkovException("Invalid varId");
        }

        val s = (varmap.get(varID)?.timeSeries) ?: throw MarkovException("Invalid varid: ${varID}")
        val i = Spliterators.spliterator(s, 0)
        val x = StreamSupport.stream(i, false)
        return x
    }

    private fun getVarIDs(): Collection<Int> {
        return varmap.keys;
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

    override public fun toString(): String {
        return "Markov{sampleCount=$sampleCount, sampleTime=$sampleTime, timeseries=$varmap}"
    }
}
