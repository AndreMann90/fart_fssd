package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.dataobjects.MCState;
import de.fssd.dataobjects.MCTransition;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Andre on 16.06.2016.
 */
public class Markov implements TimeSeries {
    private ArrayRealVector initial_states;
    private BlockRealMatrix transitions;
    private ArrayList<RealMatrix> iterations;
    private boolean stable;
    private HashMap<String, Integer> statemap;
    private Map<Integer, MCState> varmap;
    private int sampleCount;

    public static class MarkovException extends RuntimeException {
        public MarkovException(String message) {
            super(message);
        }
    }

    public Markov(FaultTree t, Map<Integer, MCState> varIDToStateMap, int sampleCount) throws MarkovException {
        this.sampleCount = sampleCount;
        stable = false;

        iterations = new ArrayList<>();
        initial_states = new ArrayRealVector(t.getChain().size());

        transitions = new BlockRealMatrix(t.getChain().size(), t.getChain().size());

        statemap = new HashMap<>();
        varmap = varIDToStateMap;

        Integer idx = 0;
        for (MCState s: t.getChain()) {
            statemap.put(s.getId(), idx);
            initial_states.setEntry(idx, s.getP0());
            idx++;
        }

        for (MCState s: t.getChain()) {
            float residual = 1.0f;
            for (MCTransition tr: s.getTransitions()) {
                transitions.setEntry(statemap.get(s.getId()), statemap.get(tr.getState()), tr.getP());
                residual -= tr.getP();
            }
            if ((residual < 0) || (residual > 1)) {
                /* Weird things happened */
                throw new MarkovException("Invalid state transition from state: " + s.getId());
            }
            transitions.setEntry(statemap.get(s.getId()), statemap.get(s.getId()), residual);
        }

        iterations.add(transitions);
    }

    private RealMatrix iterate() {
        int last = iterations.size() - 1;
        if (stable) {
            return iterations.get(last);
        }

        RealMatrix m = iterations.get(last).multiply(transitions);

        if (m.equals(iterations.get(last))) {
            stable = true;
        } else {
            iterations.add(m);
        }

        return m;
    }

    public double getVarState(int t, int varid) {
        /* Map Variable ID to entry in initial state vector */
        int idx = statemap.get(varmap.get(varid).getId());

        if (t < iterations.size()) {
            return iterations.get(t).preMultiply(initial_states).getEntry(idx);
        }

        RealMatrix m = iterations.get(0);
        for (int off = iterations.size() - 1; off < t && !stable; off++) {
            m = iterate();
        }

        return m.preMultiply(initial_states).getEntry(idx);
    }

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    public int getSamplePointsCount() {
        return sampleCount;
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    public Stream<Float> getProbabilitySeries(int varID) {
        return IntStream.range(0, this.getSamplePointsCount()).mapToObj(n->new Float(this.getVarState(n, varID)));
    }

    public boolean equalsToTimeSeries(TimeSeries timeSeries) {
        return false; // TODO: implement
    }
}
