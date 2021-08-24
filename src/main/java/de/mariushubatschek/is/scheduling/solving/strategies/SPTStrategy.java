package de.mariushubatschek.is.scheduling.solving.strategies;

import de.mariushubatschek.is.scheduling.solving.ChoiceStrategy;
import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;

/**
 * Always choose the operation with the shortest processing time first
 */
public class SPTStrategy implements ChoiceStrategy {

    @Override
    public int choose(ConstructionContext context, List<Integer> allowedOperations, List<Integer> scheduledOperations, List<Integer> unscheduledOperations) {
        int time = Integer.MAX_VALUE;
        int chosenOperation = -1;
        for (int operation : allowedOperations) {
            if (context.getProcessingTime(operation) < time) {
                time = context.getProcessingTime(operation);
                chosenOperation = operation;
            }
        }
        return chosenOperation;
    }

}
