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
        Parser p = new Parser();

        BDDBuildResult r = new BDDBuilder().build(p.parse(new File("testcases/HFTTestCase.json")));

        Pair<BDDNode, TimeSeriesFromCSV> e = TestFactory.getHFTTestCase();
        System.out.println("Expected: " + e.getValue());
        System.out.println("Actual: " + r.markov);
        assertTrue("Markovs are not equal", r.markov.equalsToTimeSeries(e.getValue()));
/*
        ArrayList<Double> vs = new ArrayList<>();

        for (Integer varId: r.stateMap.keySet()) {
            double s = markov.getVarState(1000, varId);
            vs.add(s);

            System.out.println("Var: " + r.stateMap.get(varId).getId() + " : " + s);
            Assert.assertTrue(s >= 0 && s <= 1);
        }

        Assert.assertTrue(vs.get(0) > 0);
        Assert.assertTrue(vs.get(1) > 0);
        Assert.assertTrue(vs.get(2) > 0);
        Assert.assertTrue(vs.get(3) <= Math.ulp(0));
        Assert.assertTrue(vs.get(4) > 0);
        Assert.assertTrue(vs.get(5) > 0);
*/
    }
}
