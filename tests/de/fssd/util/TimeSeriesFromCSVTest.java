package de.fssd.util;

import de.fssd.model.TimeSeries;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Andre on 23.06.2016.
 */
public class TimeSeriesFromCSVTest {
    @Test
    public void test() throws Exception {
        TimeSeries ts = new TimeSeriesFromCSV(new File("testcases/TestCaseForProgrammingProject.csv"), Arrays.asList(1,2,3,4,5,6));

    }

}