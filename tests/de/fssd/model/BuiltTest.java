package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.parser.Parser;
import de.fssd.util.TimeSeriesFromCSV;
import javafx.util.Pair;
import jdd.util.Dot;
import de.fssd.util.TestFactory;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class BuiltTest {

    @org.junit.Test
    public void parse() throws Exception {
        Parser parser = new Parser();

        FaultTree faultTree = parser.parse(new File("testcases/HFTTestCase.json"));

        System.out.println("Done parsing :)");
    }

    @org.junit.Test
    public void testBDDBuilder() throws Exception {
        Parser p = new Parser();
        FaultTree t = p.parse(new File("testcases/HFTTestCase.json"));

        BDDBuilder b = new BDDBuilder();

        BDDBuildResult r = b.build(t);
        Pair<BDDNode, TimeSeriesFromCSV> e = TestFactory.getHFTTestCase();

        System.out.println("Root nodes: " + r.rootNodes);
        assertEquals("Got an unexpected number of BDD root nodes", 1, r.rootNodes.size());
        assertEquals("BDDs are not equal", r.rootNodes.get(0), e.getKey());
        assertTrue("Markovs are not equal", r.markov.equalsToTimeSeries(e.getValue()));

        for (BDDNode rn: r.rootNodes) {
            System.out.println("Top node: " + rn.getNodeID());
        }
        Dot.setRemoveDotFile(false);
        // r.bdd.printDot("bla.dot", r.top);
    }
}