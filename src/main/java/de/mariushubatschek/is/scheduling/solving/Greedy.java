package de.mariushubatschek.is.scheduling.solving;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.OperationData;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.*;
import de.mariushubatschek.is.scheduling.modeling.test.Graph2;
import de.mariushubatschek.is.scheduling.modeling.test.GraphBasedPlan2;
import de.mariushubatschek.is.scheduling.solving.strategies.MinimalMakespanAdditionStrategy;

import java.util.*;

/**
 * Greedily choose the operation to schedule next as one which adds least to the makespan
 */
public class Greedy implements Solver {

    private ChoiceStrategy choiceStrategy;

    public Greedy(final ChoiceStrategy choiceStrategy) {
        this.choiceStrategy = choiceStrategy;
    }

    public void setChoiceStrategy(ChoiceStrategy choiceStrategy) {
        this.choiceStrategy = choiceStrategy;
    }

    @Override
    public Plan solve(final ProblemData problemData) {
        Graph2 graph = new Graph2();
        List<Integer> processingTimeList = new ArrayList<>();
        List<Integer> machinesList = new ArrayList<>();
        List<Integer> operationToJobList = new ArrayList<>();
        List<Integer> previousList = new ArrayList<>();

        //The very first node is a special source node
        processingTimeList.add(0);
        machinesList.add(-1);
        operationToJobList.add(-1);
        previousList.add(-1);

        for (JobData jobData : problemData.getJobs()) {
            graph.insertJob(jobData);
            boolean first = true;
            for (OperationData operationData : jobData.getOperations()) {
                processingTimeList.add(operationData.getDuration());
                machinesList.add(operationData.getResource());
                operationToJobList.add(jobData.getId());
                if (first) {
                    first = false;
                    previousList.add(0);
                } else {
                    previousList.add(previousList.size() - 1);
                }
            }
        }
        graph.insertDisjunctiveConstraints();

        ConstructionContext constructionContext = new ConstructionContext(processingTimeList, machinesList, operationToJobList, previousList);

        List<Integer> operations = new ArrayList<>();
        List<Integer> allowed = new ArrayList<>();
        List<Integer> scheduled = new ArrayList<>();
        for (int i = 0; i < machinesList.size(); i++) {
            operations.add(i);
            if (constructionContext.getPrevious(i) == 0) {
                allowed.add(i);
            }
        }
        List<Integer> unscheduled = new ArrayList<>(operations);
        scheduled.add(0);
        unscheduled.removeIf(o -> o == 0);

        while (scheduled.size() < operations.size()) {
            int chosenOperation = choiceStrategy.choose(constructionContext, allowed, scheduled, unscheduled);
            scheduled.add(chosenOperation);
            allowed.removeIf(o -> o == chosenOperation);
            unscheduled.removeIf(o -> o == chosenOperation);
            if (constructionContext.getNext(chosenOperation) != -1) {
                allowed.add(constructionContext.getNext(chosenOperation));
            }
        }

        scheduled.removeIf(o -> o == 0);

        graph.orientByPermutation(scheduled);

        return new GraphBasedPlan2(graph);
    }

    @Override
    public String toString() {
        return "Greedy-" + choiceStrategy.getClass().getSimpleName();
    }
}
