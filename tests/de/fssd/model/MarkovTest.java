package de.fssd.model;

import de.fssd.dataobjects.FaultTree;
import de.fssd.parser.Parser;
import javafx.util.Pair;
import jdd.bdd.BDD;

import java.io.File;
import java.io.IOException;

/**
 * Created by gbe on 6/23/16.
 */
public class MarkovTest {
    @org.junit.Test
    public void testMarkov() throws IOException {
        Parser p = new Parser();

        FaultTree t = p.parse(new File("testcases/HFTTestCase.json"));

        BDDBuilder b = new BDDBuilder();

        BDDBuildResult r = b.build(t);

        Markov m = r.m;

        for (Integer varId: r.stateMap.keySet()) {
            System.out.println("Var: " + r.stateMap.get(varId).getId() + " : " + m.getVarState(10, varId));
        }
    }
}
