package de.fssd.util;

import de.fssd.model.BDDNode;
import de.fssd.model.TimeSeries;
import javafx.util.Pair;
import jdd.bdd.BDD;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Andre on 21.06.2016.
 */
public class TestFactory {

    private static final int RAID_TEST_LENGTH = 400000;

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
            public int getSamplePointsCount() {
                return 3;
            }

            @Override
            public List<Float> getProbabilitySeries(int varID) {
                if (varID == aVar) {
                    return makeStreamForTest(0.2f, RAID_TEST_LENGTH);
                } else if (varID == bVar) {
                    return makeStreamForTest(0.2f, RAID_TEST_LENGTH);
                } else if (varID == cVar) {
                    return makeStreamForTest(0.1f, RAID_TEST_LENGTH);
                } else {
                    return null;
                }
            }
        };

        return new Pair<>(new BDDNode(bdd, NoStateDependencies.INSTANCE, top), timeSeries);
    }

    public static List<Float> getRaidTestResult() {
        return makeStreamForTest(0.136f, RAID_TEST_LENGTH);
    }

    private static List<Float> makeStreamForTest(float midValue, int midValueCount) {
        List<Float> arrayList = new ArrayList<>(midValueCount + 2);
        arrayList.add(0f);
        arrayList.addAll(Collections.nCopies(midValueCount, midValue));
        arrayList.add(1f);
        return arrayList;
    }



    /**
     * Builds the BDD and Markov that corresponds to "Programming Project – Requirements and Testing.pdf" Example
     * @return the bdd and markow
     */
    public static Pair<BDDNode, TimeSeriesFromCSV> getHFTTestCase() throws IOException {
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

        StateDependenciesBacking s = new StateDependenciesBacking(Arrays.asList(Arrays.asList(bdd.getVar(v1), bdd.getVar(v2), bdd.getVar(v3)), Arrays.asList(bdd.getVar(v4), bdd.getVar(v5), bdd.getVar(v6))));

        return new Pair<>(new BDDNode(bdd, s, top), new TimeSeriesFromCSV(new File("testcases/TestCaseForProgrammingProject.csv"), Arrays.asList(bdd.getVar(v1), bdd.getVar(v2), bdd.getVar(v3), bdd.getVar(v4), bdd.getVar(v5), bdd.getVar(v6))));
    }
}
