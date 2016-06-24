package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.MCState;
import de.fssd.dataobjects.MCTransition;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Continuous Markov implementation using uniformization
 */
public class Markov implements TimeSeries {

    private RealVector currentVector;
    private BlockRealMatrix transitionMatrix;

    private Map<Integer, McVariable> varmap;
    private int sampleCount;
    private float sampleTime;

    public static class MarkovException extends RuntimeException {
        MarkovException(String message) {
            super(message);
        }
    }

    /**
     * Represents the Variable for a MC state
     */
    private class McVariable {
        List<Float> timeSeries = new LinkedList<>();
        int orderInQ; // ordering in generator matrix Q

        McVariable(int orderInQ) {
            this.orderInQ = orderInQ;
        }

        @Override
        public String toString() {
            return timeSeries.toString();
        }
    }

    public Markov(FaultTree t, Map<Integer, MCState> varIDToStateMap) throws MarkovException {
        sampleCount = t.getSampleCount();
        sampleTime = t.getMissionTime() / (sampleCount - 1);

        final int stateCount = t.getChain().size();

        float maxRateGamma = Float.NEGATIVE_INFINITY;
        BlockRealMatrix identity = new BlockRealMatrix(stateCount, stateCount);

        currentVector = new ArrayRealVector(stateCount);
        transitionMatrix = new BlockRealMatrix(stateCount, stateCount);
        varmap = new HashMap<>();
        Map<String, Integer> nameIdToVarIdMap = new HashMap<>(); // maps the name of sate in file to varId of state given by BDD

        // manage mapping of varID that was defined by the bdd library and init the initial probability
        Integer orderInQ = 0;
        for (Integer varID : varIDToStateMap.keySet()) {
            MCState mcState = varIDToStateMap.get(varID);

            varmap.put(varID, new McVariable(orderInQ));
            nameIdToVarIdMap.put(mcState.getId(), varID);

            currentVector.setEntry(orderInQ, mcState.getP0());
            identity.setEntry(orderInQ, orderInQ, 1);
            orderInQ++;
        }

        // fill the matrix and find the maximum entry of this matrix
        for (Integer varID : varIDToStateMap.keySet()) {
            int fromOrderId = varmap.get(varID).orderInQ;

            MCState mcState = varIDToStateMap.get(varID);
            float residual = 1.0f;
            for (MCTransition tr: mcState.getTransitions()) {
                int toVarId = nameIdToVarIdMap.get(tr.getState());
                int toOrderId = varmap.get(toVarId).orderInQ;
                final float rate = tr.getP();
                if(rate > maxRateGamma) {
                    maxRateGamma = rate;
                }
                transitionMatrix.setEntry(fromOrderId, toOrderId, rate); // TODO transpose? (change from <-> to)
                residual -= rate;
            }
            if ((residual < 0) || (residual > 1)) {
                /* Weird things happened */
                throw new MarkovException("Invalid state transition from state: " + mcState.getId());
            }
            if(residual > maxRateGamma) {
                maxRateGamma = residual;
            }
            transitionMatrix.setEntry(fromOrderId, fromOrderId, residual);
        }

        // so far the transitionMatrix is the generator matrix Q, now transform to probability matrix:
        transitionMatrix.scalarMultiply(1 / maxRateGamma);
        transitionMatrix.add(identity);

        // TODO apply function to initial probabilities (rates)? (see: https://en.wikipedia.org/wiki/Uniformization_(probability_theory))

        addCurrentVectorToSeries();
        coputeTimeseries();
    }

    private void addCurrentVectorToSeries() {
        for (McVariable var : varmap.values()) {
            int index = var.orderInQ;
            final float entry = (float) currentVector.getEntry(index);
            var.timeSeries.add(entry);
        }
    }

    private void coputeTimeseries() {
        for (int i = 1; i < getSamplePointsCount(); i++) {
            RealVector nextVector = transitionMatrix.preMultiply(currentVector); // TODO is preMultiply correct?

            if(nextVector.getLInfDistance(currentVector) == 0) { // TODO maybe a threshold like 10^-6 or so
                for(; i < getSamplePointsCount(); i++) {
                    addCurrentVectorToSeries();
                }
            } else {
                currentVector = nextVector;
                addCurrentVectorToSeries();
            }
        }
    }

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    public int getSamplePointsCount() {
        return sampleCount;
    }

    public float getSampleTime() {
        return sampleTime;
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    public Stream<Float> getProbabilitySeries(int varID) {
        if(!varmap.containsKey(varID)) {
            throw new MarkovException("Invalid varId");
        }
        return varmap.get(varID).timeSeries.stream();
    }

    private Collection<Integer> getVarIDs() {
        return varmap.keySet();
    }

    boolean equalsToTimeSeries(TimeSeries timeSeries) {
        for (Integer varID : getVarIDs()) {
            List<Float> thisSeries = getProbabilitySeries(varID).collect(Collectors.toList());
            List<Float> otherSeries = timeSeries.getProbabilitySeries(varID).collect(Collectors.toList());
            if(!thisSeries.equals(otherSeries)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Markov{" +
                "sampleCount=" + sampleCount +
                ", sampleTime=" + sampleTime +
                ", timeseries=" + varmap +
                '}';
    }
}
