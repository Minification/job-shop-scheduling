package de.mariushubatschek.is.scheduling.tuning;

import de.mariushubatschek.is.scheduling.importing.Importer;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.SimulatedAnnealing;
import de.mariushubatschek.is.scheduling.solving.DisjunctiveGraph;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class SATuning {

    private List<ProblemData> problemDataList;

    public void run() throws Exception {
        problemDataList = loadData();
        Random random = new Random();
        ExecutorService executorService = Executors.newCachedThreadPool();
        int population = 5;
        List<ParameterSet> individuals = new ArrayList<>();
        for (int i = 0; i < population; i++) {
            ParameterSet parameterSet = new ParameterSet();
            parameterSet.alpha = getDoubleInRange(0.8, 0.99);
            parameterSet.initialTemperature = random.nextInt(500);
            individuals.add(parameterSet);
        }
        for (int k = 0; k < 10; k++) {
            System.out.println("Iteration: " + k);
            //Mutate individuals
            List<ParameterSet> allIndividuals = new ArrayList<>(individuals);
            for (ParameterSet individual : individuals) {
                ParameterSet parameterSet = new ParameterSet();
                parameterSet.alpha = addRandomInRange(individual.alpha, -0.05, 0.05, 0.8, 0.99);
                parameterSet.initialTemperature =
                    Math.max(0, individual.initialTemperature + random.nextInt(8)-4);
                allIndividuals.add(parameterSet);
            }
            //Evaluate fitness in parallel
            ProblemData problemData = problemDataList.get(random.nextInt(problemDataList.size()));
            Plan initialPlan = getInitialPlan(problemData);
            CountDownLatch countDownLatch = new CountDownLatch(allIndividuals.size());
            for (ParameterSet parameterSet : allIndividuals) {
                executorService.submit(() -> {
                    double fitness = 0;
                    try {
                        fitness = evaluate(initialPlan, parameterSet.alpha,
                            parameterSet.initialTemperature);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    parameterSet.fitness = fitness;
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            System.out.println("Done evaluating");
            // new population are the $population$ best individuals, i.e. those with lowest fitness
            allIndividuals.sort(Comparator.comparingDouble(v -> v.fitness));
            individuals = allIndividuals.subList(0, population);
        }
        individuals.sort(Comparator.comparingDouble(v -> v.fitness));
        ParameterSet veryBest = individuals.get(0);
        System.out.println("The best parameter set is:");
        System.out.println("alpha: " + veryBest.alpha);
        System.out.println("initialTemperature: " + veryBest.initialTemperature);
        //System.out.println("The best parameter set is:" + veryBest.fitness);
    }

    private Plan getInitialPlan(final ProblemData problemData) {
        DisjunctiveGraph disjunctiveGraph = new DisjunctiveGraph();
        return disjunctiveGraph.solve(problemData);
    }

    private double evaluate(final Plan initialPlan, final double alpha,
        final int initialTemperature) throws Exception {
        System.out.println("Start an evaluation for: " + alpha + ", " + initialTemperature);
        Random random = new Random();
        final SimulatedAnnealing sa = new SimulatedAnnealing(random, alpha, initialTemperature,
            5);
        int[] makespans = new int[10];
        CountDownLatch countDownLatch = new CountDownLatch(makespans.length);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < makespans.length; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                Plan optimizedPlan = sa.optimize(initialPlan).plan;
                makespans[finalI] = optimizedPlan.makespan();
                countDownLatch.countDown();
                System.out.println("Counting down");
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        System.out.println("End evaluation for: " + alpha + ", " + initialTemperature);
        return Arrays.stream(makespans).average().getAsDouble();
    }

    private List<ProblemData> loadData() throws Exception {
        List<ProblemData> problemDataList = new ArrayList<>();
        Importer importer = new Importer();
        Path parent = Paths.get(SATuning.class.getResource("/benchmark_problems").toURI());
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent);
        for (Path file : directoryStream) {
            problemDataList.add(importer.load(file));
        }
        return problemDataList;
    }

    /**
     * https://stackoverflow.com/a/22757471/2154149
     * @return
     */
    private double ensureRange(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private double getDoubleInRange(final double min, final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private double addRandomInRange(final double value, final double min, final double max, final double rangeMin, final double rangeMax) {
        return ensureRange(value + getDoubleInRange(min, max), rangeMin, rangeMax);
    }

    private static class ParameterSet {
        public double alpha;
        public int initialTemperature;
        public double fitness;
    }

}
