package de.mariushubatschek.is.scheduling.modeling;

import de.mariushubatschek.is.scheduling.solving.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstructionContext {

    private int[] processingTimes;
    private int[] machines;
    private int[] operationToJob;
    private int[] next;
    private int[] previous;

    public ConstructionContext(List<Integer> processingTimeList, List<Integer> machinesList, List<Integer> operationToJobList, List<Integer> previousList) {
        int representationNodes = processingTimeList.size();

        processingTimes = new int[representationNodes];
        machines = new int[representationNodes];
        operationToJob = new int[representationNodes];
        next = new int[representationNodes];
        previous = new int[representationNodes];
        for (int i = 0; i < representationNodes; i++) {
            processingTimes[i] = processingTimeList.get(i);
            machines[i] = machinesList.get(i);
            operationToJob[i] = operationToJobList.get(i);
            previous[i] = previousList.get(i);
        }


        Arrays.fill(next, -1);

        for (int i = 1; i < representationNodes; i++) {
            if (previous[i] == 0) {
                continue;
            }
            next[previous[i]] = i;
        }
    }

    public int getPrevious(final int i) {
        return previous[i];
    }

    public int getNext(final int i) {
        return next[i];
    }

    public int getProcessingTime(final int i) {
        return processingTimes[i];
    }

    public int getJob(final int i) {
        return operationToJob[i];
    }

    public int computeTourLength(final List<Integer> tour) {
        return Util.computeTourLength(tour, machines, operationToJob, processingTimes);
    }

    public int getMachine(final int i) {
        return machines[i];
    }

    public int representativeOperationsCount() {
        return processingTimes.length;
    }
}
