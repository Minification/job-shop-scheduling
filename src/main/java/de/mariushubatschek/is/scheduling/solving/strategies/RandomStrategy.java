package de.mariushubatschek.is.scheduling.solving.strategies;

import de.mariushubatschek.is.scheduling.solving.ChoiceStrategy;
import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;
import java.util.Random;

/**
 * Choose an operation at random
 */
public class RandomStrategy implements ChoiceStrategy {

    private Random random;

    public RandomStrategy(final Random random) {
        this.random = random;
    }

    @Override
    public int choose(ConstructionContext context, List<Integer> allowedOperations, List<Integer> scheduledOperations, List<Integer> unscheduledOperations) {
        int randomIndex = random.nextInt(allowedOperations.size());
        return allowedOperations.get(randomIndex);
    }
}
