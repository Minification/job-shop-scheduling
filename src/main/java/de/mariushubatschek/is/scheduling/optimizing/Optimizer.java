package de.mariushubatschek.is.scheduling.optimizing;

import de.mariushubatschek.is.scheduling.modeling.Plan;

public interface Optimizer {

    OptimizationData optimize(final Plan initialPlan);

}
