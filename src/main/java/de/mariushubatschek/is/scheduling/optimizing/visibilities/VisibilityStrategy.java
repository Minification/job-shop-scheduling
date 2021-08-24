package de.mariushubatschek.is.scheduling.optimizing.visibilities;

import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;

import java.util.List;

public interface VisibilityStrategy {

    double[] computeVisibility(final ConstructionContext constructionContext, final List<Integer> tabu, final List<Integer> allowed);

}
