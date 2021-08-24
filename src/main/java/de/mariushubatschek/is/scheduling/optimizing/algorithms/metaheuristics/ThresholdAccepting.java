package de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.AbstractOptimizer;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ThresholdAccepting extends AbstractOptimizer {

    private final double rate;

    private double threshold;

    public ThresholdAccepting() {
        super(new Random(), new MaximumIterationsStopCriterion(1000));
        this.rate = 0.95;
    }

    /*@Override
    public OptimizationData optimize(final Plan initialPlan) {
        Plan currentPlan = initialPlan;
        Plan bestPlan = initialPlan;
        int noImprovementCounter = 0;
        int bestMakeSpan = Integer.MAX_VALUE;
        double threshold = initialThreshold;
        OptimizationData optimizationData = new OptimizationData();
        while (noImprovementCounter < iterationsWithoutImprovement) {
            List<Plan> neighbourhood = currentPlan.neighbourhood(random);
            Optional<Plan> candidateOptional =
                neighbourhood.stream().reduce((a, b) -> a.makespan() < b.makespan()
                ? a : b);
            boolean improvement = false;
            if (candidateOptional.isPresent()) {
                Plan candidate = candidateOptional.get();
                if (candidate.makespan() < bestMakeSpan) {
                    bestMakeSpan = candidate.makespan();
                    bestPlan = candidate;
                }
                if (candidate.makespan() < currentPlan.makespan() + threshold) {
                    currentPlan = candidate;
                    improvement = true;
                }
            }
            threshold *= rate;
            if (!improvement) {
                noImprovementCounter++;
            } else {
                noImprovementCounter = 0;
            }
            optimizationData.progress.add(bestMakeSpan);
        }
        optimizationData.plan = bestPlan;
        return optimizationData;
    }*/

    @Override
    protected void afterChoice() {
        threshold *= rate;
    }

    @Override
    protected boolean accept(Plan candidate, Plan oldPlan) {
        return candidate.makespan() < oldPlan.makespan() + threshold;
    }

    @Override
    protected void onReset(Plan initialPLan) {
        threshold = initialPLan.makespan();
    }
}
