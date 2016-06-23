package de.fssd.evaluation;

import de.fssd.model.BDDNode;
import de.fssd.model.TimeSeries;
import de.fssd.util.TestFactory;
import javafx.util.Pair;
import jdd.bdd.BDD;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by Andre on 21.06.2016.
 */
public class EvaluationTest {

    private void testEvaluationMethods(String testCase, Evaluation evaluation, BDDNode rootNode, List<Float> expected) {
        long start = System.currentTimeMillis();
        List<Float> evaluated = evaluation.evaluateWithRootNode(rootNode);
        long end = System.currentTimeMillis();
        assertEquals(expected, evaluated);
        System.out.println(testCase + ": evaluateWithRootNode successful within " + (end-start) + "ms");

        start = System.currentTimeMillis();
        evaluated = evaluation.evaluateWithRootNodeAndComputedTable(rootNode);
        end = System.currentTimeMillis();
        assertEquals(expected, evaluated);
        System.out.println(testCase + ": evaluateWithRootNodeAndComputedTable successful within " + (end-start) + "ms");
    }

    @Test
    public void evaluateWithRootNode_Raid() throws Exception {
        Pair<BDDNode, TimeSeries> raid = TestFactory.getRAIDTest();
        BDDNode rootNode = raid.getKey();
        System.out.println(rootNode.getTreeString());
        Evaluation evaluation = new Evaluation(raid.getValue());

        testEvaluationMethods("RAID", evaluation, rootNode, TestFactory.getRaidTestResult());
    }

    @Test
    public void evaluateWithRootNode_And() throws Exception {
        final BDD bdd = new BDD(10);
        final int a = bdd.ref(bdd.createVar());
        final int b = bdd.ref(bdd.createVar());
        final int top = bdd.ref(bdd.and(a, b));
        BDDNode rootNode = new BDDNode(bdd, top);
        System.out.println(rootNode.getTreeString());

        Evaluation evaluation = new Evaluation(new TimeSeries() {
            @Override
            public int getTimeseriesCount() {
                return 3;
            }

            @Override
            public Stream<Float> getProbabilitySeries(int varID) {
                if(varID == bdd.getVar(a)) {
                    return Arrays.asList(0.0f, 0.25f, 1f).stream();
                } else if(varID == bdd.getVar(b)) {
                    return Arrays.asList(0.0f, 0.5f, 1f).stream();
                } else {
                    return null;
                }
            }
        });

        testEvaluationMethods("And", evaluation, rootNode, Arrays.asList(0f, 0.125f, 1f));
    }

    @Test
    public void evaluateWithRootNode_OneVar() throws Exception {
        final BDD bdd = new BDD(10);
        final int top = bdd.ref(bdd.createVar());
        BDDNode rootNode = new BDDNode(bdd, top);
        System.out.println(rootNode.getTreeString());

        Evaluation evaluation = new Evaluation(new TimeSeries() {
            @Override
            public int getTimeseriesCount() {
                return 3;
            }

            @Override
            public Stream<Float> getProbabilitySeries(int varID) {
                if(varID == bdd.getVar(top)) {
                    return Arrays.asList(0.0f, 0.25f, 1f).stream();
                } else {
                    return null;
                }
            }
        });

        testEvaluationMethods("OneVar", evaluation, rootNode, Arrays.asList(0f, 0.25f, 1f));
    }
}