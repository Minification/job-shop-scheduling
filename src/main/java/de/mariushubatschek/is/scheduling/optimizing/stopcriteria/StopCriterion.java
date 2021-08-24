package de.mariushubatschek.is.scheduling.optimizing.stopcriteria;

public interface StopCriterion {

    boolean isSatisfied(final int currentIteration, final int currentNoImprovementCount);

}
