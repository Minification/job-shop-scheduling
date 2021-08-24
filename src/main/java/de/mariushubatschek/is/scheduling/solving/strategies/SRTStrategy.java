package de.mariushubatschek.is.scheduling.solving.strategies;

import de.mariushubatschek.is.scheduling.solving.ChoiceStrategy;
import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;

/**
 * Always choose the operation from the job with the shortest remaining processing time
 */
public class SRTStrategy implements ChoiceStrategy {

    @Override
    public int choose(ConstructionContext context, List<Integer> allowedOperations, List<Integer> scheduledOperations, List<Integer> unscheduledOperations) {
        int time = Integer.MAX_VALUE;
        int chosenOPeration = -1;
        for (int k : allowedOperations) {
            int current = k;
            int processingTimeSum = context.getProcessingTime(current);
            while (context.getNext(current) != -1) {
                processingTimeSum += context.getProcessingTime(context.getNext(current));
                current = context.getNext(current);
            }
            if (processingTimeSum < time) {
                time = processingTimeSum;
                chosenOPeration = k;
            }
        }
        return chosenOPeration;
    }

}
