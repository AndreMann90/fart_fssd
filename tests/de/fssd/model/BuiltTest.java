package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.parser.Parser;
import de.fssd.util.TimeSeriesFromCSV;
import javafx.util.Pair;
import jdd.util.Dot;
import de.fssd.util.TestFactory;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

public class BuiltTest {

    @org.junit.Test
    public void testBDDBuilder() throws Exception {
        FaultTree t = Parser.INSTANCE.parse(new File("testcases/HFTTestCase.json"));

        BDDBuilder b = new BDDBuilder();

        BDDBuildResult r = b.build(t);
        Pair<BDDNode, TimeSeriesFromCSV> e = TestFactory.getHFTTestCase();

        assertEquals("Got an unexpected number of BDD root nodes", 1, r.getRootNodes().size());
        assertEquals("BDDs are not equal", r.getRootNodes().get(0), e.getKey());

        Dot.setRemoveDotFile(false);
        r.getBdd().printDot("bla.dot", r.getRootNodes().get(0).getNodeID());
    }
}