package de.fssd.model;

import de.fssd.parser.Parser;
import de.fssd.util.TestFactory;
import de.fssd.util.TimeSeriesFromCSV;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MarkovTest {
    @org.junit.Test
    public void testMarkov() throws IOException {
        BDDBuildResult r = new BDDBuilder().build(Parser.INSTANCE.parse(new File("testcases/HFTTestCase.json")));

        Pair<BDDNode, TimeSeriesFromCSV> e = TestFactory.getHFTTestCase();
        assertTrue("Markovs are not equal", r.getMarkov().equalsToTimeSeries(e.getValue()));
    }
}
