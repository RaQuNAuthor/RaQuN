package org.raqun.paper.raqun.similarity;

import org.raqun.paper.nwm.domain.Element;
import org.raqun.paper.raqun.data.RMatch;
import org.raqun.paper.raqun.data.RElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.raqun.paper.testhelper.TestDataFactory.*;

public class WeightMetricTest {
    WeightMetric weightMetric = new WeightMetric();

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void matchConfidenceOfEqualElementsIsTheHighest(int numberOfModels) {
        // elementA == elementB --> 1
        RElement elementA = new RElement("A", "TestElement", "0",
                getSampleProperties("prop", numberOfModels));
        RElement elementB = new RElement("B", "TestElement", "0",
                getSampleProperties("prop", numberOfModels));

        weightMetric.setNumberOfModels(numberOfModels);
        double matchConfidence = weightMetric.getMatchConfidence(elementA, elementB);
        double expectedConfidence = getNormalizationFactor(numberOfModels);
        assert doubleEquals(matchConfidence, expectedConfidence);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void matchConfidenceOfCompletelyDifferentElementsIs0(int numberOfModels) {
        // elementA !!!!= elementB --> 0
        RElement elementA = new RElement("A", "TestElement", "0", getSampleProperties());
        RElement elementB = new RElement("B", "TestElement", "0",
                getSampleProperties("abc", numberOfModels));

        weightMetric.setNumberOfModels(numberOfModels);
        double matchConfidence = weightMetric.getMatchConfidence(elementA, elementB);
        double expectedConfidence = 0.0d;
        assert doubleEquals(matchConfidence, expectedConfidence);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void matchConfidenceOfMostlyDifferentElementsIsSmall(int numberOfModels) {
        matchConfidenceOfElementsIsAsExpected(numberOfModels, 0.05);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void matchConfidenceOfHighlySimilarElementsIsHigh(int numberOfModels) {
        matchConfidenceOfElementsIsAsExpected(numberOfModels, 0.95);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 0.10, 0.25, 0.50, 0.75, 0.90, 0.99, 1})
    public void matchConfidenceIsInfluencedByRatioOfCommonProperties(double propertyRatio) {
        matchConfidenceOfElementsIsAsExpected(2, propertyRatio);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100})
    public void tuplesWithNoCommonPropertyShouldNotBeMatched(int numberOfTuples) {
        Set<RMatch> tuples = new HashSet<>();
        int modelID = 0;
        for (int i = 0; i < numberOfTuples; i++) {
            int tupleSize = getRandomInt(1, 10);
            Set<RElement> elements = new HashSet<>();
            for (int j = 0; j < tupleSize; j++) {
                List<String> properties = getSampleProperties("prop" + i + "_", 3);
                elements.add(new RElement(String.valueOf(modelID), String.valueOf(j), String.valueOf(modelID), properties));
            }
            tuples.add(new RMatch(weightMetric, elements));
        }
        assert !weightMetric.shouldMatch(tuples);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100})
    public void singleElementTuplesWithAtLeastOneCommonPropertyShouldBeMatched(int numberOfTuples) {
        Set<RMatch> tuples = new HashSet<>();
        int modelID = 0;
        for (int i = 0; i < numberOfTuples; i++) {
            Set<RElement> elements = new HashSet<>();

            List<String> properties = getSampleProperties("prop" + i + "_", 3);
            properties.add("COMMON");
            elements.add(new RElement(String.valueOf(modelID), "0", String.valueOf(modelID), properties));

            tuples.add(new RMatch(weightMetric, elements));
        }
        assert weightMetric.shouldMatch(tuples);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 0.10, 0.25, 0.50, 0.75, 0.90, 0.99, 1})
    public void tuplesShouldBeMatchedIfItDoesNotDecreaseTheWeight(double interTupleSimilarity) {
        int numberOfTuples = getRandomInt(2, 80);
        int numberOfProperties = Math.max(1, numberOfTuples / 2);
        int numberOfCommonIntraProperties = Math.max(1, (int) (getRandomRatio() * numberOfProperties));
        int numberOfCommonInterProperties = Math.max(1, (int) (interTupleSimilarity * numberOfProperties));
        int numberOfDifferentProperties = numberOfProperties - numberOfCommonInterProperties;
        List<String> commonInterProperties = getSampleProperties("common", numberOfCommonInterProperties);

        Set<RMatch> tuples = new HashSet<>();
        int modelID = 0;
        for (int i = 0; i < numberOfTuples; i++) {
            int tupleSize = getRandomInt(1, 10);
            Set<RElement> elements = new HashSet<>();
            List<String> commonIntraProperties = getSampleProperties(String.valueOf(i), numberOfCommonIntraProperties);
            for (int j = 0; j < tupleSize; j++) {
                List<String> properties = getSampleProperties("prop" + i + j + "_", numberOfDifferentProperties);
                properties.addAll(commonInterProperties);
                properties.addAll(commonIntraProperties);
                elements.add(new RElement(String.valueOf(modelID), String.valueOf(j), String.valueOf(modelID), properties));
            }
            tuples.add(new RMatch(weightMetric, elements));
        }
        double weightSum = 0.0d;
        Set<RElement> allElements = new HashSet<>();
        for (RMatch tuple : tuples) {
            allElements.addAll(tuple.getElements());
            weightSum += weightMetric.weightForElements(tuple.getElements());
        }
        double weightOfCombinedTuples = weightMetric.weightForElements(allElements);
        if (weightOfCombinedTuples < weightSum) {
            assert !weightMetric.shouldMatch(tuples);
        } else {
            assert weightMetric.shouldMatch(tuples);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void perfectMatchingHasBestQuality(int numberOfTuples) {
        int numberOfModels = getRandomInt(2, 100);
        weightMetric.setNumberOfModels(numberOfModels);
        Set<RMatch> tuples = new HashSet<>();
        int modelID = 0;
        for (int i = 0; i < numberOfTuples; i++) {
            Set<RElement> elements = new HashSet<>();
            // Add one element for each model
            for (int j = 0; j < numberOfModels; j++) {
                List<String> properties = getSampleProperties("p" + i + "_", 3);
                elements.add(new RElement(String.valueOf(modelID), String.valueOf(j), String.valueOf(modelID), properties));
            }
            tuples.add(new RMatch(weightMetric, elements));
        }
        double qualityOfMatching = weightMetric.getQualityOfMatching(tuples);
        assert doubleEquals(qualityOfMatching, numberOfTuples);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 100, 1000})
    public void unfittingMatchingHasQualityOf0(int numberOfTuples) {
        int numberOfModels = getRandomInt(2, 100);
        weightMetric.setNumberOfModels(numberOfModels);
        Set<RMatch> tuples = new HashSet<>();
        int modelID = 0;
        for (int i = 0; i < numberOfTuples; i++) {
            Set<RElement> elements = new HashSet<>();
            // Add one element for each model
            for (int j = 0; j < numberOfModels; j++) {
                List<String> properties = getSampleProperties("p" + i + j + "_", 3);
                elements.add(new RElement(String.valueOf(modelID), String.valueOf(j), String.valueOf(modelID), properties));
            }
            tuples.add(new RMatch(weightMetric, elements));
        }
        double qualityOfMatching = weightMetric.getQualityOfMatching(tuples);
        assert doubleEquals(qualityOfMatching, 0.0);
    }

    @Test
    public void qualityOfSomewhatFittingMatchingIsCorrect() {
        double expectedDisplayTupleWeight = 27.0d / 45.0d;
        double expectedMobileTupleWeight = 12.0d / 27.0d;
        double expectedMixedTupleWeight = 4.0d / 45.0d;

        Set<RMatch> tuples = getExampleMatching(weightMetric);
        // Weights were calculated by hand for each of the tuples
        double expectedWeight = expectedDisplayTupleWeight + expectedMobileTupleWeight + expectedMixedTupleWeight;
        weightMetric.setNumberOfModels(3);
        double weightOfDisplayTuple = weightMetric.weightForElements(getDisplayTuple(weightMetric).getElements());
        double weightOfMobileTuple = weightMetric.weightForElements(getMobileTuple(weightMetric).getElements());
        double weightOfMixedTuple = weightMetric.weightForElements(getMixedTuple(weightMetric).getElements());

        assert doubleEquals(weightOfDisplayTuple, expectedDisplayTupleWeight);
        assert doubleEquals(weightOfMobileTuple, expectedMobileTupleWeight);
        assert doubleEquals(weightOfMixedTuple, expectedMixedTupleWeight);

        double qualityOfMatching = weightMetric.getQualityOfMatching(tuples);

        assert doubleEquals(expectedWeight, qualityOfMatching);
    }


    // TODO: static double weightForElements(Collection<Elements> match, int numberOfModels)
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5})
    public void weightOfElementsIsCorrectWithStaticCalculation(int numberOfModels) {
        Collection<Element> displayElements = convertElementTuple(getDisplayTuple(weightMetric));
        Collection<Element> mobileElements = convertElementTuple(getMobileTuple(weightMetric));
        Collection<Element> mixedElement = convertElementTuple(getMixedTuple(weightMetric));

        double weightOfDisplayElements = WeightMetric.weightForElements(displayElements, numberOfModels);
        double weightOfMobileElements = WeightMetric.weightForElements(mobileElements, numberOfModels);
        double weightOfMixedElements = WeightMetric.weightForElements(mixedElement, numberOfModels);

        double normalizationFactor = 9.0d / (numberOfModels * numberOfModels);

        double expectedDisplayTupleWeight = (27.0d / 45.0d) * normalizationFactor;
        double expectedMobileTupleWeight = (12.0d / 27.0d) * normalizationFactor;
        double expectedMixedTupleWeight = (4.0d / 45.0d) * normalizationFactor;

        assert doubleEquals(weightOfDisplayElements, expectedDisplayTupleWeight);
        assert doubleEquals(weightOfMobileElements, expectedMobileTupleWeight);
        assert doubleEquals(weightOfMixedElements, expectedMixedTupleWeight);
    }

    private void matchConfidenceOfElementsIsAsExpected(int numberOfModels, double propertyRatio) {
        WeightMetric weightMetric = new WeightMetric(numberOfModels);
        int numberOfProperties = Math.max(1, numberOfModels / 2);
        int numberOfCommonProperties = Math.max(1, (int) (propertyRatio * numberOfProperties));
        int numberOfDifferentProperties = numberOfProperties - numberOfCommonProperties;

        List<String> commonProperties = getSampleProperties("common", numberOfCommonProperties);

        List<String> propertiesOfA = getSampleProperties("prop", numberOfDifferentProperties);
        propertiesOfA.addAll(commonProperties);

        List<String> propertiesOfB = getSampleProperties("abc", numberOfDifferentProperties);
        propertiesOfB.addAll(commonProperties);

        RElement elementA = new RElement("A", "TestElement", "0", propertiesOfA);
        RElement elementB = new RElement("B", "TestElement", "0", propertiesOfB);

        double matchConfidence = weightMetric.getMatchConfidence(elementA, elementB);
        // Expected Confidence is the normalization factor * property similarity ratio
        double expectedConfidence = getNormalizationFactor(numberOfModels) * getPropertyRatio(propertiesOfA, propertiesOfB);
        assert doubleEquals(matchConfidence, expectedConfidence);
    }

    private List<String> getSampleProperties() {
        return getSampleProperties("prop", 3);
    }

    private List<String> getSampleProperties(String base, int numberOfProperties) {
        List<String> properties = new ArrayList<>();
        for (int i = 0; i < numberOfProperties; i++) {
            properties.add(base + i);
        }
        return properties;
    }

    private double getNormalizationFactor(int numberOfModels) {
        return ((double) (2 * 2)) / (numberOfModels * numberOfModels);
    }

    private double getPropertyRatio(List<String> propertiesOfA, List<String> propertiesOfB) {
        Set<String> uniqueProperties = new HashSet<>();
        uniqueProperties.addAll(propertiesOfA);
        uniqueProperties.addAll(propertiesOfB);
        int numberOfUniqueProperties = uniqueProperties.size();
        int numberOfPropertiesOverall = propertiesOfA.size() + propertiesOfB.size();
        // This should be the number of properties that appear in both lists
        int numberOfCommonProperties = numberOfPropertiesOverall - numberOfUniqueProperties;
        return ((double) numberOfCommonProperties) / ((double) numberOfUniqueProperties);
    }

    private boolean doubleEquals(double a, double b) {
        double threshold = 0.0000001;
        return Math.abs(a - b) < threshold;
    }

    private Collection<Element> convertElementTuple(RMatch tuple) {
        Collection<Element> elements = new HashSet<>();
        for (RElement rElement : tuple.getElements()) {
            StringBuilder propertiesSB = new StringBuilder();
            for (String property : rElement.getProperties()) {
                propertiesSB.append(property);
                propertiesSB.append(";");
            }
            // Delete the last ";"
            propertiesSB.deleteCharAt(propertiesSB.length()-1);

            Element element = new Element(rElement.getUUID(), rElement.getName(),
                    propertiesSB.toString(), rElement.getModelID());
            elements.add(element);
        }
        return elements;
    }
}
