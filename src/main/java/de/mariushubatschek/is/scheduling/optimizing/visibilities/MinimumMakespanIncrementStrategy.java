package de.mariushubatschek.is.scheduling.optimizing.visibilities;

import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimumMakespanIncrementStrategy implements VisibilityStrategy {

    private double[] visibility;

    @Override
    public double[] computeVisibility(ConstructionContext constructionContext, List<Integer> tabu, List<Integer> allowed) {
        if (visibility == null || visibility.length != constructionContext.representativeOperationsCount()) {
            visibility = new double[constructionContext.representativeOperationsCount()];
        }
        Arrays.fill(visibility, 0);
        List<Integer> tabuCopy = new ArrayList<>(tabu);
        int prevTourLength = constructionContext.computeTourLength(tabu);
        for (int k : allowed) {
            tabuCopy.add(k);
            int tourLength = constructionContext.computeTourLength(tabuCopy);
            int diff = (tourLength - prevTourLength);
            visibility[k] = diff == 0 ? constructionContext.getProcessingTime(k) : 1d / diff;
            tabuCopy.removeIf(v -> v == k);
        }
        return visibility;
    }

}
