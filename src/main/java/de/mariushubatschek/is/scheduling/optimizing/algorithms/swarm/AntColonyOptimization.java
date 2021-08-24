package de.mariushubatschek.is.scheduling.optimizing.algorithms.swarm;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.OperationData;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.ConstructionContext;
import de.mariushubatschek.is.scheduling.modeling.Graph;
import de.mariushubatschek.is.scheduling.modeling.GraphBasedPlan;
import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.modeling.test.Graph2;
import de.mariushubatschek.is.scheduling.modeling.test.GraphBasedPlan2;
import de.mariushubatschek.is.scheduling.optimizing.OptimizationData;
import de.mariushubatschek.is.scheduling.optimizing.Optimizer;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.MaximumIterationsStopCriterion;
import de.mariushubatschek.is.scheduling.optimizing.stopcriteria.StopCriterion;
import de.mariushubatschek.is.scheduling.optimizing.visibilities.MinimumMakespanIncrementStrategy;
import de.mariushubatschek.is.scheduling.optimizing.visibilities.VisibilityStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Based on Dorigo & Stützle, 2004. Ant Colony Optimization.
 */
public class AntColonyOptimization implements Optimizer {

    /**
     * pheromone[x][y] means pheromone on edge from x to y
     */
    private double[][] pheromone;

    /**
     * pheromoneDeltas[x][y] means pheromone delta on edge from x to y
     */
    private double[][] pheromoneDeltas;

    private double[] probabilities;

    private boolean[][] adjacencyMatrix;

    private Random random;

    private double alpha = 1;

    private double beta = 5;

    private double Q;

    private double rho;

    private ConstructionContext constructionContext;

    private VisibilityStrategy visibilityStrategy = new MinimumMakespanIncrementStrategy();

    private int antCount;

    private int iterations;

    private int jobs;

    private Graph2 graph;

    private List<Integer> shortestTour = new ArrayList<>();

    private int shortestTourLength;

    private StopCriterion stopCriterion;

    private boolean useMinMax;

    private double min;

    private double max;

    public int getShortestTourLength() {
        return shortestTourLength;
    }

    public List<Integer> getShortestTour() {
        return shortestTour;
    }

    public void setAntCount(int antCount) {
        this.antCount = antCount;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setQ(double q) {
        Q = q;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public boolean[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
    }

    public double[][] getPheromone() {
        return pheromone;
    }

    public void setUseMinMax(boolean useMinMax) {
        this.useMinMax = useMinMax;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public AntColonyOptimization(final ProblemData problemData) {
        graph = new Graph2();
        random = new Random();
        List<Integer> processingTimeList = new ArrayList<>();
        List<Integer> machinesList = new ArrayList<>();
        List<Integer> operationToJobList = new ArrayList<>();
        List<Integer> previousList = new ArrayList<>();

        //The very first node is a special source node
        processingTimeList.add(0);
        machinesList.add(-1);
        operationToJobList.add(-1);
        previousList.add(-1);

        jobs = problemData.getJobs().size();
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

        constructionContext = new ConstructionContext(processingTimeList, machinesList, operationToJobList, previousList);

        int representationNodes = processingTimeList.size();

        //Matrices
        pheromone = new double[representationNodes][representationNodes];
        pheromoneDeltas = new double[representationNodes][representationNodes];
        probabilities = new double[representationNodes];
        adjacencyMatrix = new boolean[representationNodes][representationNodes];

        for (int i = 1; i < representationNodes; i++) {
            for (int j = 1; j < representationNodes; j++) {
                //There is a bidirectional edge between each operation that is not in the same job
                if (constructionContext.getJob(i) != constructionContext.getJob(j)) {
                    adjacencyMatrix[i][j] = true;
                    adjacencyMatrix[j][i] = true;
                }
            }
            //And a single edge from previous operation to current operation
            adjacencyMatrix[constructionContext.getPrevious(i)][i] = true;
        }

        for (int i = 0; i < representationNodes; i++) {
           // System.out.println(Arrays.toString(adjacencyMatrix[i]));
        }
        min = 0.1;
        max = 10;
    }

    private int chooseNextCity(final List<Integer> allowed, final double[] visibility, final int fromNode) {
        computeProbabilities(allowed, visibility, fromNode);
        //System.out.println("At node: " + i);
        // some conversion
        ProbabilityElement[] pes = new ProbabilityElement[probabilities.length];
        for (int p = 0; p < pes.length; p++) {
            ProbabilityElement pe = new ProbabilityElement();
            pe.index = p;
            pe.probability = probabilities[p];
            pes[p] = pe;
        }
        return chooseElement(pes).index;
    }

    /**
     * Calculate probability of going from city i to city j in allowed
     * @param allowed Set if allowed cities
     * @param visibility Visibility of each city
     * @param i Current city
     */
    private void computeProbabilities(final List<Integer> allowed, final double[] visibility, final int i) {
        Arrays.fill(probabilities, 0);
        double sum = 0;
        StringBuilder sb = new StringBuilder();
        for (int j : allowed) {
            //System.out.println("pher: " + pheromone[i][j]);
            double numerator = Math.pow(pheromone[i][j], alpha) * Math.pow(visibility[j],
                    beta);
            double denominator = 0;
            for (int k : allowed) {
                denominator += Math.pow(pheromone[i][k], alpha) * Math.pow(visibility[k],
                        beta);
            }
            //if (denominator == 0.) {
                //System.err.println("Denominator for " + j + " is 0");
                //denominator = Double.MIN_VALUE;
            //}
            probabilities[j] = numerator / denominator;
            sum += probabilities[j];
            sb.append("(" + j + ", " + probabilities[j] + ", " + numerator + ", " + denominator + "), ");
            //System.out.println("prob: " + probabilities[j] + ", n: " + numerator + ", d: " + denominator);
        }
        if (sum == 0) {
            System.err.println("Sum is 0!!!");
            System.err.println(allowed);
            System.err.println(sb.toString());
            System.exit(-1);
        }
       // System.out.println(Arrays.toString(probabilities));
        //print(probabilities);
    }

    private double cap(double d) {
        return Math.max(d, Double.MIN_VALUE);
    }

    private double getPertubedVisibilityFor(final double[] visibility, final int i) {
        if (visibility[i] == 0.) {
            //System.err.println("Visibility for " + i + " is 0");
            return Double.MIN_VALUE;
        }
        return visibility[i];
    }

    /**
     * https://stackoverflow.com/a/6737362/2154149
     * @param elements
     * @return
     */
    private ProbabilityElement chooseElement(final ProbabilityElement[] elements) {
        int idx = 0;
        for (double r = random.nextDouble(); idx < elements.length - 1; ++idx) {
            r -= elements[idx].probability;
            if (r <= 0.0) break;
        }
        return elements[idx];
    }

    private ProbabilityElement chooseElement2(final ProbabilityElement[] elements) {
        int idx = 0;
        double sumProb = 0;
        for (ProbabilityElement e : elements) {
            sumProb += e.probability;
        }
        //System.out.println("Sum: " + sumProb);
        double r = ThreadLocalRandom.current().nextDouble(sumProb + Double.MIN_VALUE);
        double p = elements[idx].probability;
        while (p < r) {
            idx++;
            p += elements[idx].probability;
        }
        return elements[idx];
    }

    private void print(final double[][] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.println(Arrays.toString(v[i]));
        }
        System.out.println();
    }

    @Override
    public OptimizationData optimize(Plan initialPlan) {
        antCount = jobs;
        int bestAnt = -1;
        shortestTourLength = Integer.MAX_VALUE;
        shortestTour = new ArrayList<>();
        OptimizationData optimizationData = new OptimizationData();
        //Nodes yet to visit
        Map<Integer, List<Integer>> G = new HashMap<>();
        //Allowed nodes
        Map<Integer, List<Integer>> S = new HashMap<>();
        //tabu lists
        Map<Integer, List<Integer>> tabu = new HashMap<>();
        Map<Integer, Integer> lengths = new HashMap<>();
        for (int i = 0; i < antCount; i++) {
            G.put(i, new ArrayList<>());
            S.put(i, new ArrayList<>());
            tabu.put(i, new ArrayList<>());
            lengths.put(i, 0);
        }

        //initialize pheromones to antCount / makespan of initial plan, as stated in Dorigo et al.
        for (int i = 0; i < pheromone.length; i++) {
            for (int j = 0; j < pheromone[i].length; j++) {
                pheromone[i][j] = antCount / (double) initialPlan.makespan();
            }
        }

        stopCriterion = new MaximumIterationsStopCriterion(iterations);

        int NC = 0;
        while (!stopCriterion.isSatisfied(NC, 0)) {
            //System.err.println(instance + ", useMinMax=" + useMinMax + ", it=" + NC);
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            //System.out.println("Iteration: " + NC);
            for (int k = 0; k < antCount; k++) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                G.get(k).clear();
                S.get(k).clear();
                tabu.get(k).clear();
                lengths.put(k, 0);
                // All except 0 may initially be visited
                for (int i = 1; i < adjacencyMatrix.length; i++) {
                    G.get(k).add(i);
                }
                //All transitions from 0 are initially allowed
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    if (adjacencyMatrix[0][j]) {
                        S.get(k).add(j);
                    }
                }
            }
            //Step 2
            for (int k = 0; k < antCount; k++) {
                tabu.get(k).add(0);
            }
            //Step 3
            boolean allAntsDone = false;
            boolean[] antsDone = new boolean[antCount];
            while (!allAntsDone) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                for (int k = 0; k < antCount; k++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }
                    if (antsDone[k]) {
                        continue;
                    }

                    double[] visibility = visibilityStrategy.computeVisibility(constructionContext, tabu.get(k), S.get(k));

                    int i = tabu.get(k).get(tabu.get(k).size() - 1); // i is last element in tabu
                    final int j = chooseNextCity(S.get(k), visibility, i);
                    //System.err.println("k: " + k + " chose " + j);
                    //System.out.println("Ant: " + k + ", chosen node: " + j);
                    //Add j to tabu, remove j from allowed and unvisited nodes
                    tabu.get(k).add(j);
                    S.get(k).removeIf(e -> e == j);
                    G.get(k).removeIf(e -> e == j);
                    //Add immediate successor of j to allowed nodes
                    if (constructionContext.getNext(j) != -1) {
                        S.get(k).add(constructionContext.getNext(j));
                    }
                    //System.err.println("k: " + k + ", " + S.get(k));
                    if (tabu.get(k).size() > 1 && tabu.get(k).get(tabu.get(k).size() - 1).equals(tabu.get(k).get(tabu.get(k).size() - 2))) {
                        System.err.println("k: " + k);
                        System.err.println(tabu.get(k));
                        System.err.println(tabu.get(k).get(tabu.get(k).size() - 1));
                        System.err.println("Added twice");
                        System.exit(-1);
                    }
                    //System.out.println("Allowed now: " + S.get(k));
                    antsDone[k] = tabu.get(k).size() == adjacencyMatrix.length;
                }
                allAntsDone = true;
                for (int k = 0; k < antCount; k++) {
                    if (!antsDone[k]) {
                        allAntsDone = false;
                        break;
                    }
                }
            }
            //Step 4
            //Find best stuff
            int iterationBestAnt = -1;
            int iterationBestTourLength = Integer.MAX_VALUE;
            List<Integer> iterationBestTour = null;
            for (int k = 0; k < antCount; k++) {
                //System.out.println("Ant " + k);
                //System.out.println("Tour: " + tabu.get(k));
                int tourLength = constructionContext.computeTourLength(tabu.get(k));
                //System.out.println("Tourlength " + tourLength);
                lengths.put(k, tourLength);
                if (tourLength < shortestTourLength) {
                    //System.out.println("New best");
                    bestAnt = k;
                    shortestTourLength = tourLength;
                    shortestTour = new ArrayList<>(tabu.get(k));
                }
                if (tourLength < iterationBestTourLength) {
                    iterationBestAnt = k;
                    iterationBestTourLength = tourLength;
                    iterationBestTour = new ArrayList<>(tabu.get(k));
                }
            }

            //Setup pheromone deltas
            if (useMinMax) {
                List<Integer> tour_k = tabu.get(iterationBestAnt);
                for (int m = 0; m < tour_k.size() - 1; m++) {
                    int i = tour_k.get(m);
                    int j = tour_k.get(m+1);
                    if (adjacencyMatrix[i][j]) {
                        pheromoneDeltas[i][j] += 1d / lengths.get(iterationBestAnt);
                    }
                }
            } else {
                for (int k = 0; k < antCount; k++) {
                    List<Integer> tour_k = tabu.get(k);
                    for (int m = 0; m < tour_k.size() - 1; m++) {
                        int i = tour_k.get(m);
                        int j = tour_k.get(m+1);
                        if (adjacencyMatrix[i][j]) {
                            pheromoneDeltas[i][j] += Q / lengths.get(k);
                        }
                    }
                }
            }

            //Step 5
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                    if (useMinMax) {
                        pheromone[i][j] = Math.min(max, Math.max(min, rho * pheromone[i][j] + pheromoneDeltas[i][j]));
                    } else {
                        //System.err.println("deltas: " + pheromoneDeltas[i][j]);
                        pheromone[i][j] = rho * pheromone[i][j] + pheromoneDeltas[i][j];
                    }

                    //System.out.println(pheromoneDeltas[i][j]);
                    //Reset pheromone delta
                    pheromoneDeltas[i][j] = 0;
                }
            }

            NC++;
            optimizationData.progress.add(shortestTourLength);
        }
        //System.out.println(instance + ", useMinMax=" + useMinMax + ", startToPlan");
        optimizationData.plan = toPlan(shortestTour);
        //System.out.println(instance + ", useMinMax=" + useMinMax + ", afterToPlan");
        return optimizationData;
    }

    /**
     * Convert a tour to a plan
     * @param tour
     * @return
     */
    private Plan toPlan(List<Integer> tour) {
        List<Integer> permutation = new ArrayList<>(tour);
        permutation.removeIf(o -> o == 0);
        //System.out.println(instance + ", useMinMax=" + useMinMax + ", startOrienting");
        graph.orientByPermutation(permutation);
        //System.out.println(instance + ", useMinMax=" + useMinMax + ", doneOrienting");
        return new GraphBasedPlan2(graph);
    }

    private static class ProbabilityElement {
        public int index;
        public double probability;
    }

    @Override
    public String toString() {
        if (useMinMax)
            return "MinMaxAntColonyOptimization";
        else
            return "AntColonyOptimization";
    }

    private String instance;

    public void setInstance(String instance) {
        this.instance = instance;
    }

}
