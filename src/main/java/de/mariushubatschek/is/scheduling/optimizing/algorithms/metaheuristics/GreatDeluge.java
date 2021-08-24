package de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.AbstractOptimizer;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GreatDeluge extends AbstractOptimizer {

    private double initialWater;

    private double initialRain;

    private double waterLevel;

    private double rain;

    public GreatDeluge() {
        super(new Random(), new MaximumIterationsStopCriterion(1000));
    }

    public GreatDeluge(final int initialWater, final int rain,
        final int iterationsWithoutImprovement, final Random random) {
        super(random, new MaximumIterationsStopCriterion(10000));
        this.initialRain = rain;
    }

    /*@Override
    public OptimizationData optimize(final Plan initialPlan) {
        Plan currentPlan = initialPlan;
        Plan bestPlan = initialPlan;
        int noImprovementCount = 0;
        int bestMakeSpan = currentPlan.makespan();
        double rain = initialRain;
        double waterLevel = currentPlan.makespan();
        OptimizationData optimizationData = new OptimizationData();
        while (noImprovementCount < iterationsWithoutImprovement) {
            List<Plan> candidatePlans = currentPlan.neighbourhood(random);
            //TODO: Make following step configurable
            //Find best candidate plan
            Optional<Plan> optionalPlan = candidatePlans.stream()
                .reduce((a, b) -> a.makespan() < b.makespan() ? a : b);
            boolean improvement = false;
            if (optionalPlan.isPresent()) {
                Plan candidate = optionalPlan.get();

                if (candidate.makespan() < waterLevel) {
                    currentPlan = candidate;
                    improvement = true;
                    if (candidate.makespan() < bestMakeSpan) {
                        bestPlan = candidate;
                        bestMakeSpan = bestPlan.makespan();
                    }
                }
            }
            waterLevel -= rain;
            if (!improvement) {
                noImprovementCount++;
            } else {
                noImprovementCount = 0;
            }
            optimizationData.progress.add(bestMakeSpan);
        }
        optimizationData.plan = bestPlan;
        return optimizationData;
    }*/

    @Override
    protected void afterChoice() {
        waterLevel -= rain;
    }

    @Override
    protected boolean accept(Plan candidate, Plan oldPlan) {
        return candidate.makespan() < waterLevel;
    }

    @Override
    protected void onReset(Plan initialPlan) {
        waterLevel = initialPlan.makespan();
        rain = 1 / waterLevel;
    }

}
