package org.raqun.paper.experiments.common;

import java.nio.file.Paths;

public class ExperimentSetup {
    public final String name;
    public final int numberOfRepeats;
    public final String datasetName;
    public final String resultFile;
    public final String mergeResultFile;
    public final String datasetFile;
    public final int chunkSize;

    public ExperimentSetup(String name, int numberOfRepeats,
                           String resultDir, String datasetDir,
                           String datasetName, int chunkSize) {
        this.name = name;
        this.numberOfRepeats = numberOfRepeats;
        this.datasetName = datasetName;
        // Adjust name for ArgoUML subsets
        if (datasetName.startsWith("argouml_p")) {
            String[] parts = datasetName.split("_");
            String tempName = parts[0] + "_" + parts[1];
            this.resultFile = Paths.get(resultDir, name, name + "_" + tempName + "_stats.json").toString();
        } else {
            this.resultFile = Paths.get(resultDir, name, name + "_" + datasetName + "_stats.json").toString();
        }
        this.mergeResultFile = Paths.get(resultDir, name, name + "_" + datasetName + "_model.csv").toString();
        this.datasetFile = Paths.get(datasetDir, datasetName + ".csv").toString();
        this.chunkSize = chunkSize;
    }

}
