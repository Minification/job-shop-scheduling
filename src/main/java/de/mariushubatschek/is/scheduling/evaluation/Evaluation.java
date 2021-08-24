package de.mariushubatschek.is.scheduling.evaluation;

import de.mariushubatschek.is.scheduling.importing.Importer;
import de.mariushubatschek.is.scheduling.importing.ProblemData;
import de.mariushubatschek.is.scheduling.modeling.Plan;
import de.mariushubatschek.is.scheduling.optimizing.*;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.SimulatedAnnealing;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.ThresholdAccepting;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.GreatDeluge;
import de.mariushubatschek.is.scheduling.optimizing.algorithms.metaheuristics.AdaptiveGreatDeluge;
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
import java.util.stream.Collectors;

public class Evaluation {

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
        List<Instantiator<Optimizer>> optimizerInstantiators = Arrays.asList(
                () -> new SimulatedAnnealing(),
                () -> new ThresholdAccepting(),
                () -> new GreatDeluge(),
                () -> new AdaptiveGreatDeluge()
        );
        Path loggingPath = Paths.get(System.getProperty("user.dir"), "output");
        for (File f : loggingPath.toFile().listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
        DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(Evaluation.class.getResource("/benchmark_problems").toURI()), filter);
        for (Path path : paths) {
            System.out.println(path.getFileName());
            ExecutorService executorService = Executors.newCachedThreadPool();
            ProblemData problemData = new Importer().load(path);
            for (Instantiator<Solver> solverInstantiator : solverInstantiators) {
                Solver solver = solverInstantiator.create();
                Plan initialPlan = solver.solve(problemData);
                CountDownLatch countDownLatch = new CountDownLatch(optimizerInstantiators.size() * randomIterations);
                for (Instantiator<Optimizer> optimizerInstantiator : optimizerInstantiators) {
                    for (int i = 1; i <= randomIterations; i++) {
                        Optimizer optimizer = optimizerInstantiator.create();
                        int finalI = i;
                        executorService.submit(() -> {
                            OptimizationData optimizationData = optimizer.optimize(initialPlan);
                            try {
                                Logging.log(optimizationData.progress, FilenameUtils.getBaseName(path.getFileName().toString()) + "_" + solver.toString() + "_" + optimizer.getClass().getSimpleName() + "_" + finalI, loggingPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            countDownLatch.countDown();
                        });
                    }
                }
                countDownLatch.await();
            }
            executorService.shutdown();
        }

    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        new Evaluation().run();
    }

}
