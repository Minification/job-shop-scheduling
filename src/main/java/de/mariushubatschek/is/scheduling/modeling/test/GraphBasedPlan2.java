package de.mariushubatschek.is.scheduling.modeling.test;

import de.mariushubatschek.is.scheduling.modeling.Graph;
import de.mariushubatschek.is.scheduling.modeling.GraphBasedPlan;
import de.mariushubatschek.is.scheduling.modeling.Plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GraphBasedPlan2 implements Plan {

    private Graph2 disjunctiveGraph;

    public GraphBasedPlan2(final Graph2 disjunctiveGraph) {
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
        return disjunctiveGraph.flipEligibleEdge(random)
                .stream()
                .map(GraphBasedPlan2::new)
                .collect(Collectors.toList());
    }

}
