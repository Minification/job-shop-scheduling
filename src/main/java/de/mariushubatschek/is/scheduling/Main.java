package de.mariushubatschek.is.scheduling;

import de.mariushubatschek.is.scheduling.importing.Importer;
import de.mariushubatschek.is.scheduling.importing.ProblemData;

import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.AdaptiveGreatDeluge;
import de.mariushubatschek.is.scheduling.solving.Greedy;
import de.mariushubatschek.is.scheduling.solving.strategies.RandomStrategy;

import java.nio.file.Paths;
import java.util.Random;

public class Main {

    private static final String FT06 = "/benchmark_problems/ft06_6_jobs_6_resources.json";

    private static final String FT20 = "/benchmark_problems/ft20_20_jobs_5_resources.json";

    private static final String ORB04 = "/benchmark_problems/orb04_10_jobs_10_resources.json";

    private static final String ACRO = "/small_problems/acrogenesis.json";

    public static void main(String[] args) throws Exception {
        Importer importer = new Importer();
        ProblemData load = importer.load(Paths.get(Main.class.getResource(FT20).toURI()));
        /*
        Plan plan3 = new DisjunctiveGraph().solve(load);
        System.out.println(plan3);
        System.out.println(plan3.makespan());
        System.out.println(plan3.isValid());*/
        Plan plan4 = new Greedy(new RandomStrategy(new Random())).solve(load);
        System.out.println(plan4.makespan());
        Plan optimizedPlan;

        /*SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing();
        System.out.println("Makespan of plan 3: " + plan4.makespan());
        OptimizationData optimizationData = simulatedAnnealing.optimize(plan4);
        optimizedPlan = optimizationData.plan;
        System.out.println("After simulated annealing: " + optimizedPlan.makespan());*/
        /*ThresholdAccepting thresholdAccepting = new ThresholdAccepting();
        optimizedPlan = thresholdAccepting.optimize(plan4).plan;
        System.out.println("After threshold accepting: " + optimizedPlan.makespan());*/
        /*GreatDeluge greatDeluge = new GreatDeluge();
        optimizedPlan = greatDeluge.optimize(plan4).plan;
        System.out.println("After great deluge: " + optimizedPlan.makespan());*/
        AdaptiveGreatDeluge adaptiveGreatDeluge = new AdaptiveGreatDeluge();
        optimizedPlan = adaptiveGreatDeluge.optimize(plan4).plan;
        System.out.println("After adaptive great deluge: " + optimizedPlan.makespan());

        /*AntColonyOptimization antColonyOptimization = new AntColonyOptimization(load);
        antColonyOptimization.setIterations(3000);
        antColonyOptimization.setController(null);
        antColonyOptimization.setAlpha(1);
        antColonyOptimization.setBeta(5);
        antColonyOptimization.setQ(20);
        antColonyOptimization.setRho(0.7);
        antColonyOptimization.optimize(plan3);*/
    }

}
