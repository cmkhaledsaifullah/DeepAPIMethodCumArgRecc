package org.srlab.damca.simplename;

import java.util.ArrayList;
import java.util.List;

public class OutputEntity
{
    String testCase;
    String sourcePath;
    String sourcePosition;
    String context = "";
    String actualOutput = "";
    List<String> predictedOutput;

    public OutputEntity(String testCase, String sourcePath, String sourcePosition, String context, String actualOutput, List<String> predictedOutput) {
        this.testCase = testCase;
        this.sourcePath = sourcePath;
        this.sourcePosition = sourcePosition;
        this.context = context;
        this.actualOutput = actualOutput;
        this.predictedOutput = predictedOutput;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(String sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public List<String> getPredictedOutput() {
        return predictedOutput;
    }

    public void setPredictedOutput(List<String> predictedOutput) {
        this.predictedOutput = predictedOutput;
    }
}
