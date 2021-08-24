package de.mariushubatschek.is.scheduling.modeling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphBasedPlan implements Plan {

    private Graph disjunctiveGraph;

    public GraphBasedPlan(final Graph disjunctiveGraph) {
        this.disjunctiveGraph = disjunctiveGraph;
    }

    @Override
    public int makespan() {
        return disjunctiveGraph.makespan();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public List<Plan> neighbourhood(final Random random) {
        List<Plan> neighbourHood = new ArrayList<>();
        neighbourHood.add(new GraphBasedPlan(disjunctiveGraph.flipEligibleEdge(random)));
        return neighbourHood;
    }

}
