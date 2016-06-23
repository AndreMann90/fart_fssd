package de.fssd;

import de.fssd.dataobjects.FaultTree;
import de.fssd.evaluation.Evaluation;
import de.fssd.model.BDDBuildResult;
import de.fssd.model.BDDBuilder;
import de.fssd.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            Parser parser = new Parser();
            FaultTree faultTree = parser.parse(new File("testcases/HFTTestCase.json"));
            BDDBuildResult result = new BDDBuilder().build(faultTree);
            Evaluation evaluation = new Evaluation(result.m);
            List<Float> topEventSeries = evaluation.evaluateWithRootNodeAndComputedTable(result.b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
