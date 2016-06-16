package tests;

import de.fssd.dataobjects.FaultTree;
import de.fssd.model.BDDBuilderResult;
import de.fssd.parser.Parser;
import de.fssd.model.BDDBuilder;
import jdd.bdd.BDD;
import jdd.util.Dot;

import java.io.File;

/**
 * Created by Andre on 16.06.2016.
 */
public class Test {
    @org.junit.Test
    public void parse() throws Exception {
        Parser parser = new Parser();

        FaultTree faultTree = parser.parse(new File("testcases/test1.json"));

        System.out.println("Done parsing :)");
    }

    @org.junit.Test
    public void testBDDBuilder() throws Exception {
        Parser p = new Parser();
        FaultTree t = p.parse(new File("testcases/test1.json"));

        BDDBuilder b = new BDDBuilder();

        BDDBuilderResult r = b.build(t);

        System.out.println("Top node: " + r.top);
        Dot.setRemoveDotFile(false);
        r.bdd.printDot("bla.dot", r.top);
    }
}