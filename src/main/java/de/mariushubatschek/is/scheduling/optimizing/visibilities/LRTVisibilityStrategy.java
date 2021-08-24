package de.mariushubatschek.is.scheduling.optimizing.visibilities;

import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.Arrays;
import java.util.List;

public class LRTVisibilityStrategy implements VisibilityStrategy {

    private double[] visibility;

    @Override
    public double[] computeVisibility(ConstructionContext constructionContext, List<Integer> tabu, List<Integer> allowed) {
        if (visibility == null || visibility.length != constructionContext.representativeOperationsCount()) {
            visibility = new double[constructionContext.representativeOperationsCount()];
        }
        Arrays.fill(visibility, 0);
        for (int k : allowed) {
            int current = k;
            double processingTimeSum = constructionContext.getProcessingTime(current);
            while (constructionContext.getNext(current) != -1) {
                processingTimeSum += constructionContext.getProcessingTime(constructionContext.getNext(current));
                current = constructionContext.getNext(current);
            }
            visibility[k] = processingTimeSum;
        }
        return visibility;
    }

}
