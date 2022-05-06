package com.jkrude.tedris.teded.TimeTravel;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SameParameterValue")
public class TimeTravelRiddle {

  //https://www.youtube.com/watch?v=ukUPojrPFPA&list=PLJicmE8fK0EiFRt1Hm5a_7SJFaikIFW30&index=33
  /*
   * Graph problem
   * infinite options -> partially computable
   * Only one solution
   *
   * 1. The graph is fully connected.
   * 2. The graph is bi-colored.
   * 3. The color of an edge is random.
   * 4. The graph needs to have a circle of size 3 were every edge has the same color
   *
   * 1. Generate fully connected graph with k vertices
   * 2. Create every possible coloring
   * 3. If every coloring yields a same-colored-three-circle return Solution
   */

  public static void main(String[] args) {
    measure(() ->
            runExperiment(5, true),
        1
    );
  }

  static void measure(Runnable toBeMeasured, int executionTimes) {
    long[] measurements = new long[executionTimes];
    for (int i = 0; i < executionTimes; i++) {
      long startTime = System.currentTimeMillis();
      toBeMeasured.run();
      long endTime = System.currentTimeMillis();
      measurements[i] = (endTime - startTime);
    }
    double avg = Arrays.stream(measurements).average().orElse(0);
    System.out.println(avg + "ms");
  }


  /*
   * Version 2:  use boolean cut search tree size in half
   */
  static void runExperiment(int circleLength) {
    runExperiment(circleLength, Runtime.getRuntime().availableProcessors(), false);
  }

  static void runExperiment(int circleLength, boolean print) {
    runExperiment(circleLength, Runtime.getRuntime().availableProcessors(), print);

  }

  static void runExperiment(int circleLength, int numOfProcessors, boolean print) {
    // Number of vertices
    if (circleLength < 3) {
      throw new IllegalArgumentException("Circle has to be of length 3 or more");
    }
    for (int k = 3; k <= 10; ++k) {
      long searchSpace = (long) (Math.pow(2, (k * (k - 1)) / 2f) / 2f);
      if (print) {
        System.out.println("Testing for k= " + k);
        System.out.println("Maximum search space: " + searchSpace);
      }
      long sizePerJob = (long) 1e7;
      boolean allHadCycle = executeJobs(searchSpace, numOfProcessors, sizePerJob, circleLength, k);
      if (allHadCycle) {
        if (print) {
          System.out.println("Found solution: k = " + k);
        }
        return;
      }
    }
    if (print) {
      System.out.println("No solution found");
    }
  }

  static boolean executeJobs(
      long searchSpace,
      int numThreads,
      long sizePerJob,
      int cycleLength,
      int numNodes
  ) {
    int numJobs = (int) Math.ceil(searchSpace / (float) sizePerJob);
    var jobs = new SearchWorker[numJobs];
    System.out.println("Using " + numJobs + " jobs.");
    // Distribute search space into partitions of sizePerJob.
    for (int i = 0; i < numJobs; i++) {
      long start = i * sizePerJob;
      long end = i != numThreads - 1 ? (i + 1) * sizePerJob : searchSpace;
      jobs[i] = new SearchWorker(start, end, cycleLength, numNodes);
    }
    var executor = new ThreadPoolExecutor(
        numThreads,
        numThreads,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingDeque<>()
    );
    var completionService = new ExecutorCompletionService<Optional<Long>>(executor);
    try {
      // Queue jobs
      for (SearchWorker job : jobs) {
        completionService.submit(job);
      }
      // Analyze results as they come in.
      for (int waitingFor = jobs.length; waitingFor > 0; --waitingFor) {
        // Wait for next future to complete.
        var optResult = completionService.take().get();
        if (optResult.isPresent()) {
          executor.shutdownNow(); // TODO does not stop current threads.
          System.out.println("Found graph without cycle: " + optResult.get());
          return false;
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
