package de.mariushubatschek.is.scheduling.solving;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    /**
     * Zhang2006, Figure 5
     * J. Zhang, X. Hu, X. Tan, J.H. Zhong, Q. Huang. Implementation of an Ant Colony Optimization technique for job shop scheduling problem. In: Transactions of the institute of Measurement and Control 28, 1. (2006). pp. 93 - 108.
     * @param tour
     * @return
     */
    public static int computeTourLength(final List<Integer> tour, final int[] machines, final int[] operationToJob, final int[] processingTimes) {
        Map<Integer, Integer> M_time = new HashMap<>();
        Map<Integer, Integer> J_time = new HashMap<>();
        for (int i = 0; i < tour.size(); i++) {
            if (tour.get(i) != 0) {
                M_time.put(machines[tour.get(i)], 0);
                J_time.put(operationToJob[tour.get(i)], 0);
            }
        }

        for (int tourNode : tour) {
            if (tourNode != 0) {
                int t = Math.max(M_time.get(machines[tourNode]), J_time.get(operationToJob[tourNode]));
                int newTime = t + processingTimes[tourNode];
                M_time.put(machines[tourNode], newTime);
                J_time.put(operationToJob[tourNode], newTime);
            }
        }

        int max = Integer.MIN_VALUE;
        for (int machine : M_time.keySet()) {
            int time = M_time.get(machine);
            if (time > max) {
                max = time;
            }
        }
        return max;
    }

}
