package de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.AbstractOptimizer;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;

import java.util.Random;

public class SimulatedAnnealing extends AbstractOptimizer {

    private double alpha;

    private int initialTemperature;

    private int temperature;

    public SimulatedAnnealing() {
        super(new MaximumIterationsStopCriterion(1000));
        this.alpha = 0.999;
        this.initialTemperature = 1000;
    }

    public SimulatedAnnealing(final Random random, final double alpha, final int initialTemperature,
                              final int maximumNoImprovementCount) {
        super(new MaximumIterationsStopCriterion(1000000));
        this.alpha = alpha;
        this.initialTemperature = initialTemperature;
    }

    /*@Override
    public OptimizationData optimize(final Plan initialPlan) {
        Plan oldPlan = initialPlan;
        Plan bestPlan = initialPlan;
        double temperature = initialTemperature;
        double alpha = initialAlpha;
        int noImprovementCounter = 0;
        OptimizationData optimizationData = new OptimizationData();
        while (noImprovementCounter < maximumNoImprovementCount) {
            List<Plan> candidatePlans = oldPlan.neighbourhood(random);
            //TODO: Make following step configurable
            //Find best candidate plan
            Optional<Plan> optionalPlan = candidatePlans.stream()
                .reduce((a, b) -> a.makespan() < b.makespan() ? a : b);

            boolean improvement = false;
            if (optionalPlan.isPresent()) {
                Plan neighbour = optionalPlan.get();
                int difference = neighbour.makespan() - oldPlan.makespan();
                //We check for less than since we want to minimize
                if (difference < 0) {
                    oldPlan = neighbour;
                    improvement = true;
                    //Record best plan seen so far
                    if (neighbour.makespan() < bestPlan.makespan()) {
                        bestPlan = neighbour;
                    }
                } else {
                    double randomValue = this.random.nextDouble();
                    if (randomValue < acceptance(difference, temperature)) {
                        oldPlan = neighbour;
                    }
                }
            }
            if (!improvement) {
                temperature *= alpha;
                noImprovementCounter++;
            } else {
                noImprovementCounter = 0;
            }
            optimizationData.progress.add(bestPlan.makespan());
        }
        optimizationData.plan = bestPlan;
        return optimizationData;
    }*/

    @Override
    protected void afterChoice() {
        temperature *= alpha;
    }

    @Override
    protected boolean accept(Plan candidate, Plan oldPlan) {
        double randomValue = this.random.nextDouble();
        return randomValue < acceptance(candidate.makespan(), oldPlan.makespan(), temperature);
    }

    @Override
    protected void onReset(Plan initialPlan) {
        this.temperature = initialTemperature;
    }

    private double acceptance(final int candidate, final int old, final double temperature) {
        if (candidate - old <= 0) {
            return 1;
        }
        double exponent = -(candidate - old) / temperature;
        return Math.exp(exponent);
    }

}
