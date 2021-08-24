package de.mariushubatschek.is.scheduling.optimizing;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.StopCriterion;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class AbstractOptimizer implements Optimizer {

    private int noImprovementCounter;

    private int currentIteration;

    private StopCriterion stopCriterion;

    protected final Random random;

    private Plan currentPlan;

    private Plan bestPlan;

    private int bestMakeSpan;

    public AbstractOptimizer() {
        this(new Random(), new MaximumIterationsStopCriterion(100));
    }

    public AbstractOptimizer(final StopCriterion stopCriterion) {
        this(new Random(), stopCriterion);
    }

    public AbstractOptimizer(final Random random) {
        this(random, new MaximumIterationsStopCriterion(100));
    }

    public AbstractOptimizer(final Random random, final StopCriterion stopCriterion) {
        this.stopCriterion = stopCriterion;
        this.random = random;
    }

    @Override
    public OptimizationData optimize(Plan initialPlan) {
        reset(initialPlan);
        OptimizationData optimizationData = new OptimizationData();
        while (!stopCriterion.isSatisfied(currentIteration, noImprovementCounter)) {
            List<Plan> neighbourhood = currentPlan.neighbourhood(random);
            Optional<Plan> candidateOptional = chooseCandidate(neighbourhood);
            boolean improvement = false;
            if (candidateOptional.isPresent()) {
                Plan candidate = candidateOptional.get();
                if (accept(candidate, currentPlan)) {
                    if (candidate.makespan() < bestMakeSpan) {
                        bestMakeSpan = candidate.makespan();
                        bestPlan = candidate;
                        improvement = true;
                    }
                    currentPlan = candidate;
                }
            }
            afterChoice();
            if (!improvement) {
                noImprovementCounter++;
            } else {
                noImprovementCounter = 0;
            }
            currentIteration++;
            optimizationData.progress.add(bestMakeSpan);
        }
        optimizationData.plan = bestPlan;
        return optimizationData;
    }

    private void reset(final Plan initialPlan) {
        currentPlan = initialPlan;
        bestPlan = initialPlan;
        bestMakeSpan = initialPlan.makespan();
        currentIteration = 0;
        noImprovementCounter = 0;
        onReset(initialPlan);
    }

    private Optional<Plan> chooseCandidate(final List<Plan> candidates) {
        return candidates
                .stream()
                .reduce((a, b) -> a.makespan() < b.makespan() ? a : b);
    }

    protected abstract void afterChoice();

    protected abstract boolean accept(final Plan candidate, final Plan oldPlan);

    protected abstract void onReset(final Plan initialPlan);

}
