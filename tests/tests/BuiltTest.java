package tests;

import de.fssd.dataobjects.FaultTree;
import de.fssd.model.BDDNode;
import de.fssd.model.Markov;
import de.fssd.parser.Parser;
import de.fssd.model.BDDBuilder;
import javafx.util.Pair;
import jdd.util.Dot;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

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

        Pair<BDDNode, Markov> r = b.build(t);
        Pair<BDDNode, Markov> e = TestFactory.getHFTTestCase();

        assertEquals("BDDs are not equal", r.getKey(), e.getKey());
        assertEquals("Markovs are not equal", r.getValue(), e.getValue()); //TODO: implement markov and its equal method

        System.out.println("Top node: " + r.getKey().getNodeID());
        Dot.setRemoveDotFile(false);
        // r.bdd.printDot("bla.dot", r.top);
    }
}