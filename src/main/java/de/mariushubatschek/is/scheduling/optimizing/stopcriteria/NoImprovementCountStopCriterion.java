package de.mariushubatschek.is.scheduling.optimizing.stopcriteria;

public class NoImprovementCountStopCriterion implements StopCriterion {

    private int maximumNoImprovementCount;

    public NoImprovementCountStopCriterion(int maximumNoImprovementCount) {
        this.maximumNoImprovementCount = maximumNoImprovementCount;
    }

    @Override
    public boolean isSatisfied(int currentIteration, int currentNoImprovementCount) {
        return currentNoImprovementCount > maximumNoImprovementCount;
    }
}
