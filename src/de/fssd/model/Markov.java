package de.fssd.model;

import de.fssd.dataobjects.MCState;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Andre on 16.06.2016.
 */
public class Markov implements TimeSeries {

    public Markov(Map<Integer, MCState> varIDToStateMap) {
        //TODO
    }

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    public int getTimeseriesCount() {
        return 0; //TODO
    }

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    public Stream<Float> getProbabilitySeries(int varID) {
        return null; //TODO
    }

    public boolean equalsToTimeSeries(TimeSeries timeSeries) {
        return false; // TODO: implement
    }
}
