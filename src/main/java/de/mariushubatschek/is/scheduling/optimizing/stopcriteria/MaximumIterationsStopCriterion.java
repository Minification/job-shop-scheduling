package de.mariushubatschek.is.scheduling.optimizing.stopcriteria;

public class MaximumIterationsStopCriterion implements StopCriterion {

    private final int maximumIteration;

    public MaximumIterationsStopCriterion(int maximumIteration) {
        this.maximumIteration = maximumIteration;
    }

    @Override
    public boolean isSatisfied(int currentIteration, int currentNoImprovementCount) {
        return currentIteration > maximumIteration;
    }

}
