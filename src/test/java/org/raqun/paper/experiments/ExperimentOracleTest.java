package org.raqun.paper.experiments;

import org.raqun.paper.raqun.data.RElement;
import org.raqun.paper.experiments.common.ExperimentOracle;
import org.raqun.paper.raqun.data.RMatch;
import org.raqun.paper.raqun.similarity.WeightMetric;
import org.raqun.paper.raqun.similarity.SimilarityFunction;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ExperimentOracleTest {
    private String[] propertiesA = {"a1", "a2"};
    private String[] propertiesB = {"b1", "b2"};
    private String[] propertiesC = {"c1", "c2"};

    private SimilarityFunction similarityFunction = new WeightMetric();

    // Model 1
    private RElement m1A = getNodeA("1");
    private RElement m1B = getNodeB("1");
    private RElement m1C = getNodeC("1");

    // Model 2
    private RElement m2A = getNodeA("2");
    private RElement m2B = getNodeB("2");
    private RElement m2C = getNodeC("2");

    // Model 3
    private RElement m3A = getNodeA("3");
    private RElement m3B = getNodeB("3");
    private RElement m3C = getNodeC("3");

    // Model 4
    private RElement m4A = getNodeA("4");
    private RElement m4B = getNodeB("4");
    private RElement m4C = getNodeC("4");


    private RElement getNodeA(String modelID) {
        // Class A
        String groundTruthA = "aaa";
        String nameA = "A";
        return new RElement(modelID, groundTruthA, nameA, Arrays.asList(propertiesA));
    }
    private RElement getNodeB(String modelID) {
        // Class B
        String groundTruthB = "bbb";
        String nameB = "B";
        return new RElement(modelID, groundTruthB, nameB, Arrays.asList(propertiesB));
    }
    private RElement getNodeC(String modelID) {
        // Class C
        String groundTruthC = "ccc";
        String nameC = "C";
        return new RElement(modelID, groundTruthC, nameC, Arrays.asList(propertiesC));
    }

    private RMatch getTuple(RElement... nodes) {
        return new RMatch(similarityFunction, nodes);
    }

    private void check(Set<RMatch> merge, int tp, int fp, int fn, double precision, double recall) {
        ExperimentOracle oracle = new ExperimentOracle(merge);
        // Temp variable for easier debugging
        int oracleTP = (int) oracle.getTp();
        int oracleFP = (int) oracle.getFp();
        int oracleFN = (int) oracle.getFn();
        double oraclePrecision = oracle.getPrecision();
        double oracleRecall = oracle.getRecall();

        assert oracleTP == tp;
        assert oracleFP == fp;
        assert oracleFN == fn;
        assert Double.compare(oraclePrecision, precision) == 0;
        assert Double.compare(oracleRecall, recall) == 0;
    }

    @Test
    void completeCorrectMergeTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A, m2A, m3A, m4A));
        merge.add(getTuple(m1B, m2B, m3B, m4B));
        merge.add(getTuple(m1C, m2C, m3C, m4C));

        check(merge, 18, 0, 0, 1, 1);
    }

    @Test
    void nothingMergedTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A));
        merge.add(getTuple(m2A));
        merge.add(getTuple(m3A));
        merge.add(getTuple(m4A));
        merge.add(getTuple(m1B));
        merge.add(getTuple(m2B));
        merge.add(getTuple(m3B));
        merge.add(getTuple(m4B));
        merge.add(getTuple(m1C));
        merge.add(getTuple(m2C));
        merge.add(getTuple(m3C));
        merge.add(getTuple(m4C));

        check(merge,0, 0, 18, 0, 0);
    }

    @Test
    void everythingMergedIncorrectlyTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A, m4B, m3C));
        merge.add(getTuple(m1B, m2C, m3A));
        merge.add(getTuple(m4C, m2A, m3B));

        check(merge, 0, 9, 9, 0, 0);
    }

    @Test
    void singleClassesNotMergedTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A, m2A, m3A));
        merge.add(getTuple(m1B, m3B, m4B));
        merge.add(getTuple(m2C, m3C, m4C));
        merge.add(getTuple(m4A));
        merge.add(getTuple(m2B));
        merge.add(getTuple(m1C));

        check(merge, 9, 0, 9, 1, 0.5);
    }

    @Test
    void mergeWithSmallMistakesTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A, m2B, m3C, m4A));
        merge.add(getTuple(m1B, m2A, m3B, m4B));
        merge.add(getTuple(m1C, m2C, m3A, m4C));

        check(merge, 7, 11, 11, 7.0/18.0, 7.0/18.0);
    }

    @Test
    void twoClassesShareATupleTwo() {
        Set<RMatch> merge = new HashSet<>();
        merge.add(getTuple(m1A, m2A, m3C, m4C));
        merge.add(getTuple(m1B, m2B, m3B, m4B));
        merge.add(getTuple(m1C, m3A));

        check(merge, 8, 5, 4, 8.0/13.0, 8.0/12.0);
    }
}
