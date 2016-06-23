package de.fssd.util;

import de.fssd.model.BDDNode;
import de.fssd.model.TimeSeries;
import javafx.util.Pair;
import jdd.bdd.BDD;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by Andre on 21.06.2016.
 */
public class TestFactory {

    /**
     * Builds the BDD and Markov that corresponds to the Faulttree for the RAID in the Programming Project Intro Slides
     * @return the bdd
     */
    public static Pair<BDDNode, TimeSeries> getRAIDTest() {
        BDD bdd = new BDD(20);

        int a = bdd.ref(bdd.createVar()); // disc 1
        int b = bdd.ref(bdd.createVar()); // disc 2
        int c = bdd.ref(bdd.createVar()); // raid controller

        int ab = bdd.ref(bdd.and(a, b));
        int top = bdd.ref(bdd.or(ab, c));
        bdd.deref(ab);

        final int aVar = bdd.getVar(a);
        final int bVar = bdd.getVar(b);
        final int cVar = bdd.getVar(c);
        TimeSeries timeSeries = new TimeSeries() {
            @Override
            public int getTimeseriesCount() {
                return 3;
            }

            @Override
            public Stream<Float> getProbabilitySeries(int varID) {
                if (varID == aVar) {
                    return Arrays.asList(0f, 0.2f, 1f).stream();
                } else if (varID == bVar) {
                    return Arrays.asList(0f, 0.2f, 1f).stream();
                } else if (varID == cVar) {
                    return Arrays.asList(0f, 0.1f, 1f).stream();
                } else {
                    return null;
                }
            }
        };

        return new Pair<>(new BDDNode(bdd, top), timeSeries);
    }

    /**
     * Builds the BDD and Markov that corresponds to "Programming Project – Requirements and Testing.pdf" Example
     * @return the bdd and markow
     */
    public static Pair<BDDNode, TimeSeries> getHFTTestCase() {
        BDD bdd = new BDD(20);

        int v1 = bdd.ref(bdd.createVar());
        int v2 = bdd.ref(bdd.createVar());
        int v3 = bdd.ref(bdd.createVar());
        int v4 = bdd.ref(bdd.createVar());
        int v5 = bdd.ref(bdd.createVar());
        int v6 = bdd.ref(bdd.createVar());

        int n11 = bdd.ref(bdd.or(v1, v2));
        int n12 = bdd.ref(bdd.and(n11, v4));
        int n13 = bdd.ref(bdd.and(v2, v5));
        int n14 = bdd.ref(bdd.or(bdd.or(n12, v3), n13));
        int n15 = bdd.ref(bdd.and(n14, n13));
        int top = bdd.ref(bdd.and(n15, v6));

        /* Map<Integer, MCState> varIDToStateMap = new HashMap<>();
        varIDToStateMap.put(bdd.getVar(v1), new MCState("1", 0.9f, Collections.emptyList(), Collections.singletonList(new MCTransition(0.2f, "2"))));
        varIDToStateMap.put(bdd.getVar(v2), new MCState("2", 0.1f, Collections.emptyList(), Arrays.asList(new MCTransition(0.1f, "1"), new MCTransition(0.1f, "3"))));
        varIDToStateMap.put(bdd.getVar(v3), new MCState("3", 0.0f, Collections.emptyList(), Collections.singletonList(new MCTransition(0.05f, "2"))));
        varIDToStateMap.put(bdd.getVar(v4), new MCState("4", 1.0f, Collections.emptyList(), Collections.singletonList(new MCTransition(0.1f, "5"))));
        varIDToStateMap.put(bdd.getVar(v5), new MCState("5", 0.0f, Collections.emptyList(), Collections.singletonList(new MCTransition(0.05f, "6"))));
        varIDToStateMap.put(bdd.getVar(v5), new MCState("6", 0.0f, Collections.emptyList(), Collections.emptyList())); */

        return new Pair<>(new BDDNode(bdd, top), new TimeSeriesFromCSV(new File("testcases/TestCaseForProgrammingProject.csv")));
    }
}