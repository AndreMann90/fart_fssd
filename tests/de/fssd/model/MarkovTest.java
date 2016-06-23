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

        System.out.println(m);

        for (Integer varId: r.stateMap.keySet()) {
            System.out.println(m.getVarState(1000, varId));
        }
    }
}