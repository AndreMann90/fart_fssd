package de.fssd.util;

import de.fssd.model.TimeSeries;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class TimeSeriesFromCSVTest {
    @Test
    public void test() throws Exception {
        TimeSeries ts = new TimeSeriesFromCSV(new File("testcases/TestCaseForProgrammingProject.csv"), Arrays.asList(1,2,3,4,5,6));
        assertEquals(81, ts.getSamplePointsCount());
    }

}