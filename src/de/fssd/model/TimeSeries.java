package de.fssd.model;

import java.util.stream.Stream;

/**
 * Created by Andre on 21.06.2016.
 */
public interface TimeSeries {

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    int getTimeseriesCount();

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * @return timeseries
     */
    Stream<Float> getProbabilitySeries(int varID);
}
