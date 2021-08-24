package de.mariushubatschek.is.scheduling.solving;

import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;

public interface ChoiceStrategy {

    int choose(final ConstructionContext context, final List<Integer> allowedOperations, final List<Integer> scheduledOperations, final List<Integer> unscheduledOperations);

}
