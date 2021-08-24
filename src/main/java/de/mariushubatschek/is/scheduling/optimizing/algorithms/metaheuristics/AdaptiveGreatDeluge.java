package de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.OptimizationData;
import de.mariushubatschek.is.scheduling.optimizing.Optimizer;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.StopCriterion;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AdaptiveGreatDeluge implements Optimizer {

    private StopCriterion stopCriterion = new MaximumIterationsStopCriterion(0);

    @Override
    public OptimizationData optimize(final Plan initialPlan) {
        Random random = new Random();
        Plan s = initialPlan;
        Plan sBest = initialPlan;
        double DV = sBest.makespan();
        int I = 1000;
        double B0 = sBest.makespan();
        double B = B0;
        double decayRate = B0 - DV / I;
        double alpha = 3;
        decayRate = decayRate + decayRate * alpha;
        int noImprovementCounter = 0;
        double beta = 0;
        int W = 100;
        OptimizationData optimizationData = new OptimizationData();
        while (!stopCriterion.isSatisfied(-I, noImprovementCounter)) {
            List<Plan> candidatePlans = s.neighbourhood(random);
            //TODO: Make following step configurable
            //Find best candidate plan
            Optional<Plan> optionalPlan = candidatePlans.stream()
                .reduce((a, b) -> a.makespan() < b.makespan() ? a : b);
            if (optionalPlan.isPresent()) {
                Plan sStar = optionalPlan.get();
                if (sStar.makespan() <= s.makespan() || sStar.makespan() <= B) {
                    s = sStar;
                    if (sStar.makespan() <= sBest.makespan()) {
                        sBest = sStar;
                        beta = 0;
                    }
                } else {
                    noImprovementCounter++;
                }
                B = B - decayRate;
                if (noImprovementCounter > W && B <= sBest.makespan()) {
                    s = sBest;
                    if (s.makespan() <= DV) {
                        DV = s.makespan() * 0.9;
                    }
                    s = sStar;
                    beta = beta + 0.0005;
                    B = s.makespan() + s.makespan() * beta;
                    decayRate = B - DV/I;
                    alpha = alpha * 0.9;
                    decayRate = decayRate * alpha;
                }
            } else {
                noImprovementCounter++;
            }
            I--;
            optimizationData.progress.add(sBest.makespan());
        }
        optimizationData.plan = sBest;
        return optimizationData;
    }

}
