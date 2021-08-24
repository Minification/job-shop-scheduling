package de.mariushubatschek.is.scheduling.solving.strategies;

import de.mariushubatschek.is.scheduling.solving.ChoiceStrategy;
import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.ArrayList;
import java.util.List;

public class MinimalMakespanAdditionStrategy implements ChoiceStrategy {

    @Override
    public int choose(ConstructionContext context, List<Integer> allowedOperations, List<Integer> scheduledOperations, List<Integer> unscheduledOperations) {
        List<Integer> tabuCopy = new ArrayList<>(scheduledOperations);
        int chosen = -1;
        int chosenLength = Integer.MAX_VALUE;
        for (int k : allowedOperations) {
            tabuCopy.add(k);
            int tourLength = context.computeTourLength(tabuCopy);
            if (tourLength < chosenLength) {
                chosen = k;
                chosenLength = tourLength;
            }
            tabuCopy.removeIf(v -> v == k);
        }
        return chosen;
    }

}
