package de.fssd.util;

import de.fssd.model.TimeSeries;

import java.io.File;
import java.util.stream.Stream;

/**
 * Created by Andre on 22.06.2016.
 */
public class TimeSeriesFromCSV implements TimeSeries {

    public TimeSeriesFromCSV(File file) {

    }

    @Override
    public int getTimeseriesCount() {
        return 0;
    }

    @Override
    public Stream<Float> getProbabilitySeries(int varID) {
        return null;
    }
}
