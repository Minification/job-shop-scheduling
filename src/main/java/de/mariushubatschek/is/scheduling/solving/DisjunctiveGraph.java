package de.mariushubatschek.is.scheduling.solving;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.importing.ResourceData;
import de.mariushubatschek.is.scheduling.modeling.EdgeType;
import de.mariushubatschek.is.scheduling.modeling.Graph;
import de.mariushubatschek.is.scheduling.modeling.GraphBasedPlan;
import de.mariushubatschek.is.scheduling.modeling.Operation;
import de.mariushubatschek.is.scheduling.modeling.OriginalPlan;
import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.modeling.Vertex;
import de.mariushubatschek.is.scheduling.modeling.VertexType;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisjunctiveGraph implements Solver {

    @Override
    public Plan solve(final ProblemData problemData) {
        Graph graph = new Graph();
        for (JobData jobData : problemData.getJobs()) {
            graph.insertJob(jobData);
        }
        graph.insertDisjunctiveConstraints();
        /*Deque<Vertex> sort = graph.sort();
        Map<Integer, List<Operation>> map = new HashMap<>();
        for (ResourceData resourceData : problemData.getResources()) {
            map.put(resourceData.getId(), new ArrayList<>());
        }
        List<Operation> operations = new ArrayList<>();
        int last = -1;
        for (Vertex v : sort) {
            if (v.vertexType == VertexType.OPERATION) {
                Operation operation = new Operation();
                operation.setJobIndex(v.job);
                operation.setResource(v.machine);
                operation.setDuration(v.duration);
                operation.setStartTime(last + 1);
                operation.setIndex(v.index);
                last = operation.getEndTime();
                map.get(v.machine).add(operation);
                operations.add(operation);
            }
        }

        for (Operation operation : operations) {
            if (operation.getIndex() > 0) {
                Operation previousOperation = operations.stream().filter(
                    o -> o.getJobIndex() == operation.getJobIndex() && o.getIndex() < operation
                        .getIndex()).findFirst().get();
                operation.setPreviousOperation(previousOperation);
            }
        }*/

        return new GraphBasedPlan(graph);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

