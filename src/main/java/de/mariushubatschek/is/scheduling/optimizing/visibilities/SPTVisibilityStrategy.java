package de.mariushubatschek.is.scheduling.optimizing.visibilities;

import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;

public class SPTVisibilityStrategy implements VisibilityStrategy {

    private double[] visibility;

    @Override
    public double[] computeVisibility(ConstructionContext constructionContext, List<Integer> tabu, List<Integer> allowed) {
        if (visibility == null || visibility.length != constructionContext.representativeOperationsCount()) {
            visibility = new double[constructionContext.representativeOperationsCount()];
            for (int i = 0; i < visibility.length; i++) {
                visibility[i] = 1d / constructionContext.getProcessingTime(i);
            }
        }
        return visibility;
    }
}
