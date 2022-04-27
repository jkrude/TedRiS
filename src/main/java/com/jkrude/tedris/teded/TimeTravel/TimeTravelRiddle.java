package com.jkrude.tedris.teded.TimeTravel;

import com.jkrude.tedris.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.paukov.combinatorics3.Generator;

@SuppressWarnings("SameParameterValue")
public class TimeTravelRiddle {

  private static final int CIRCLE_LENGTH = 3;

  //https://www.youtube.com/watch?v=ukUPojrPFPA&list=PLJicmE8fK0EiFRt1Hm5a_7SJFaikIFW30&index=33
  /*
   * Graph problem
   * infinite options -> partially computable
   * Only one solution
   *
   * 1. The graph is fully connected.
   * 2. The edges of the graph are bi-colored.
   * 3. The color of an edge is random.
   * 4. The graph needs to have a circle of size 3 were every edge has the same color
   *
   * 1. Generate fully connected graph with k vertices
   * 2. Create every possible coloring
   * 3. If every coloring yields a same-colored-three-circle return Solution
   */

  public static void main(String[] args) {
    measure(() -> runExperimentV2(5, true), 1);
//    measure(() -> runExperimentV2(5, 1), 10);

  }

  static void measure(Runnable toBeMeasured, int executionTimes) {
    Long[] measurements = new Long[executionTimes];
    for (int i = 0; i < executionTimes; i++) {
      long startTime = System.currentTimeMillis();
      toBeMeasured.run();
      long endTime = System.currentTimeMillis();
      measurements[i] = (endTime - startTime);
    }
    double avg = Arrays.stream(measurements).reduce(0L, Long::sum) / (float) executionTimes;
    System.out.println(avg + "ms");
  }


  static boolean tryAllWithXEdges(boolean[][] currGraphMatrix, int edges) {
    List<Pair<Integer, Integer>> toBeMapped = new ArrayList<>();
    for (int i = 0; i < currGraphMatrix.length; i++) {
      for (int j = i + 1; j < currGraphMatrix.length; j++) {
        toBeMapped.add(new Pair<>(i, j));
      }
    }
    long counter = 0;
    long startTime = System.currentTimeMillis();
    for (List<Pair<Integer, Integer>> opt : Generator.combination(toBeMapped).simple(edges)) {
      var coloredGraph = colorGraph(currGraphMatrix, opt);
      if (containsNoCircle(coloredGraph)) {
        System.out.println("Found one without cycle: " + Arrays.deepToString(coloredGraph));
        return false;
      }
      counter++;
      if (counter % 1000000000 == 0) {
        System.out.println("Reached " + counter + "in " + (System.currentTimeMillis() - startTime));
      }
    }
    System.out.println("All possibilities had a cycle");
    return true;
  }

  /*
   * Version 2:  use boolean cut search tree size in half
   */
  static int runExperimentV2(int circleLength) {
    return runExperimentV2(circleLength, Runtime.getRuntime().availableProcessors(), false);
  }

  static int runExperimentV2(int circleLength, boolean print) {
    return runExperimentV2(circleLength, Runtime.getRuntime().availableProcessors(), print);

  }

  static int runExperimentV2(int circleLength, int numOfProcessors, boolean print) {
    // Number of vertices
    if (circleLength < 3) {
      throw new IllegalArgumentException("Circle has to be of length 3 or more");
    }
    for (int k = 3; k <= 10; ++k) {
      long searchSpace = (long) (Math.pow(2, (k * (k - 1)) / 2f) / 2f);
      long sizePerP = (long) Math.ceil(searchSpace / (float) numOfProcessors);
      if (print) {
        System.out.println("Testing for k= " + k);
        System.out.println("Maximum search space: " + searchSpace);
        System.out.println("Using " + numOfProcessors + " threads.");
        System.out.println("Search space per process: " + sizePerP);
      }
      SearchThread[] threads = new SearchThread[numOfProcessors];
      SearchCallback callback = new SearchCallback(threads, print);
      for (int i = 0; i < numOfProcessors; i++) {
        long start = i * sizePerP;
        long end = i != numOfProcessors - 1 ? (i + 1) * sizePerP : searchSpace;
        threads[i] = new SearchThread(circleLength, k, start, end, callback);
      }

      for (SearchThread thread : threads) {
        thread.start();
      }
      for (Thread thread : threads) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (callback.allGraphsHadCycles()) {
        return k;
      }
    }
    if (print) {
      System.out.println("No solution found");
    }
    return -1;
  }


  static boolean[][] createMatrixOfSize(int k) {
    boolean[][] graphMatrix = new boolean[k][k];
    for (int i = 0; i < k; i++) {
      graphMatrix[i][i] = false;
    }
    return graphMatrix;
  }

  static List<Pair<Integer, Integer>> toBeMapped(int k) {
    List<Pair<Integer, Integer>> toBeMapped = new ArrayList<>();
    for (int i = 0; i < k; i++) {
      for (int j = i + 1; j < k; j++) {
        toBeMapped.add(new Pair<>(i, j));
      }
    }
    return toBeMapped;
  }

  static boolean[][] colorGraph(boolean[][] currGraphMatrix,
      List<Pair<Integer, Integer>> edges) {
    for (var row : currGraphMatrix) {
      Arrays.fill(row, false);
    }
    for (Pair<Integer, Integer> edge : edges) {
      currGraphMatrix[edge.getX()][edge.getY()] = true;
      currGraphMatrix[edge.getY()][edge.getX()] = true;
    }
    return currGraphMatrix;
  }

  static boolean[][] colorGraph(boolean[][] currGraphMatrix, boolean[] choices,
      List<Pair<Integer, Integer>> pairs) {
    assert choices.length == pairs.size();
    for (int i = 0; i < pairs.size(); i++) {
      currGraphMatrix[pairs.get(i).getX()][pairs.get(i).getY()] = choices[i];
      currGraphMatrix[pairs.get(i).getY()][pairs.get(i).getX()] = choices[i];
    }
    return currGraphMatrix;
  }

  static boolean containsNoCircle(boolean[][] graphMatrix) {
    return containsNoCircle(graphMatrix, false);
  }

  static boolean containsNoCircle(boolean[][] graphMatrix, boolean inverted) {
    for (int i = 0; i < graphMatrix.length; i++) {
      for (int j = 0; j < graphMatrix.length; j++) {
        if (graphMatrix[i][j] ^ inverted) {
          List<Integer> path = new ArrayList<>(CIRCLE_LENGTH * 2);
          path.add(i);
          path.add(j);
          boolean result = testForCircleRecursive(graphMatrix, path, inverted);
          if (result) {
            return false;
          }
        }
      }
    }
    return true;
  }

  static boolean testForCircleRecursive(boolean[][] graphMatrix, List<Integer> path, boolean inverted) {
    assert CIRCLE_LENGTH >= 1;
    assert path.size() <= CIRCLE_LENGTH;
    assert path.size() >= 1;

    int lastVisitedNode = path.get(path.size() - 1);
    if (path.size() == CIRCLE_LENGTH) {
      return graphMatrix[lastVisitedNode][path.get(0)];
    }

    for (int i = 0; i < graphMatrix.length; ++i) {
      if ((graphMatrix[lastVisitedNode][i] ^ inverted) && !path.contains(i)) {
        List<Integer> nextPath = new ArrayList<>(path);
        nextPath.add(i);
        var result = testForCircleRecursive(graphMatrix, nextPath, inverted);
        if (result) {
          return true; // otherwise continue with next i
        }
      }
    }
    return false;
  }


  static class SearchCallback {

    public final boolean print;
    private final SearchThread[] threads;
    private boolean allGraphsHadCycles;

    SearchCallback(SearchThread[] threads) {
      this(threads, false);
    }

    SearchCallback(SearchThread[] threads, boolean print) {
      this.threads = threads;
      this.allGraphsHadCycles = true;
      this.print = print;
    }

    synchronized void reportResult(boolean[][] graph, long triedGraphs) {
      for (SearchThread thread : this.threads) {
        thread.interrupt();
      }
      this.allGraphsHadCycles = false;
      if (this.print) {
        System.out.println("After " + triedGraphs + " iterations, found instance without cycle: ");
        System.out.println(Arrays.deepToString(graph));
      }
    }

    synchronized void reportResult(long triedGraphs) {
      if (this.print) {
        System.out.println("All graphs had cycles. Tested: " + triedGraphs);
      }
    }

    public boolean allGraphsHadCycles() {
      return this.allGraphsHadCycles;
    }
  }

}
