package de.mariushubatschek.is.scheduling.evaluation;

import de.mariushubatschek.is.scheduling.importing.Importer;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.OptimizationData;
import de.mariushubatschek.is.scheduling.optimizing.Optimizer;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.AdaptiveGreatDeluge;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.GreatDeluge;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.SimulatedAnnealing;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.ThresholdAccepting;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.swarm.AntColonyOptimization;
import de.mariushubatschek.is.scheduling.solving.DisjunctiveGraph;
import de.mariushubatschek.is.scheduling.solving.Greedy;
import de.mariushubatschek.is.scheduling.solving.Solver;
import de.mariushubatschek.is.scheduling.solving.strategies.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class EvaluationSwarm {
    public void run() throws URISyntaxException, IOException, InterruptedException {
        List<String> instanceStrings = Arrays.asList(
                "ft06",
                "abz8",
                "la35",
                "orb06",
                "swv13"
        );
        String filter = "*{" + String.join(",", instanceStrings) + "}*.json";
        int randomIterations = 5;
        List<Instantiator<Solver>> solverInstantiators = Arrays.asList(
                () -> new DisjunctiveGraph(),
                () -> new Greedy(new RandomStrategy(new Random())),
                () -> new Greedy(new MinimalMakespanAdditionStrategy()),
                () -> new Greedy(new LPTStrategy()),
                () -> new Greedy(new LRTStrategy()),
                () -> new Greedy(new LRMStrategy()),
                () -> new Greedy(new SPTStrategy()),
                () -> new Greedy(new SRTStrategy())
        );
        List<Function<ProblemData,AntColonyOptimization>> optimizerInstantiators = Arrays.asList(
                (p) -> {
                    AntColonyOptimization object =new AntColonyOptimization(p);
                    object.setIterations(1000);
                    object.setAntCount(10);
                    object.setAlpha(1);
                    object.setRho(0.7);
                    object.setBeta(5);
                    object.setQ(10);
                    object.setUseMinMax(false);
                    return object;
                },
                (p) -> {
                    AntColonyOptimization object = new AntColonyOptimization(p);
                    object.setIterations(1000);
                    object.setAntCount(10);
                    object.setAlpha(1);
                    object.setRho(0.7);
                    object.setBeta(5);
                    object.setQ(10);
                    object.setUseMinMax(true);
                    object.setMax(10);
                    object.setMin(0.1);
                    return object;
                }
        );
        Path loggingPath = Paths.get(System.getProperty("user.dir"), "outputACO");
        if (!Files.exists(loggingPath)){
            Files.createDirectory(loggingPath);
        }
        for (File f : loggingPath.toFile().listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(Evaluation.class.getResource("/benchmark_problems").toURI()), filter);
        for (Path path : paths) {
            System.out.println(path.getFileName());

            ProblemData problemData = new Importer().load(path);
            for (Instantiator<Solver> solverInstantiator : solverInstantiators) {
                Solver solver = solverInstantiator.create();
                Plan initialPlan = solver.solve(problemData);
                CountDownLatch countDownLatch = new CountDownLatch(optimizerInstantiators.size() * randomIterations);
                for (Function<ProblemData,AntColonyOptimization> optimizerInstantiator : optimizerInstantiators) {
                    for (int i = 1; i <= randomIterations; i++) {
                        AntColonyOptimization optimizer = optimizerInstantiator.apply(problemData);
                        optimizer.setInstance(FilenameUtils.getBaseName(path.getFileName().toString()));
                        int finalI = i;
                        executorService.submit(() -> {
                            //System.out.println("Starting " + FilenameUtils.getBaseName(path.getFileName().toString()) + "_" + solver.toString() + "_" + optimizer.toString() + "_" + finalI);
                            OptimizationData optimizationData = optimizer.optimize(initialPlan);
                            //System.out.println("Done " + FilenameUtils.getBaseName(path.getFileName().toString()) + "_" + solver.toString() + "_" + optimizer.toString() + "_" + finalI);
                            try {
                                Logging.log(optimizationData.progress, FilenameUtils.getBaseName(path.getFileName().toString()) + "_" + solver.toString() + "_" + optimizer.toString() + "_" + finalI, loggingPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            countDownLatch.countDown();
                        });
                    }
                }
                //System.out.println("Awaiting...");
                countDownLatch.await();
                //System.out.println("Done awaiting.");
            }
        }
        executorService.shutdown();
    }
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        new EvaluationSwarm().run();
    }
}
