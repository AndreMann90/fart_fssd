package de.fssd.model;

import de.fssd.parser.Parser;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MarkovTest {
    @org.junit.Test
    public void testMarkov() throws IOException {
        Parser p = new Parser();

        BDDBuildResult r = new BDDBuilder().build(p.parse(new File("testcases/HFTTestCase.json")));

        Markov m = r.m;

        ArrayList<Float> vs = new ArrayList<>();

        for (Integer varId: r.stateMap.keySet()) {
            Float s = m.getVarState(1000, varId);
            vs.add(s);

            System.out.println("Var: " + r.stateMap.get(varId).getId() + " : " + s);
            Assert.assertTrue(s >= 0 && s <= 1);
        }

        Assert.assertTrue(vs.get(0) > 0);
        Assert.assertTrue(vs.get(1) > 0);
        Assert.assertTrue(vs.get(2) > 0);
        Assert.assertTrue(vs.get(3) == 0);
        Assert.assertTrue(vs.get(4) > 0);
        Assert.assertTrue(vs.get(5) > 0);
    }
}
