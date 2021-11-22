package teded;

import core.BinarySequentialSearch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.paukov.combinatorics3.Generator;
import util.Pair;

public class TimeTravelRiddle {

  private static int CIRCLE_LENGTH = 3;

  //https://www.youtube.com/watch?v=ukUPojrPFPA&list=PLJicmE8fK0EiFRt1Hm5a_7SJFaikIFW30&index=33
  /*
   * Graph problem
   * infinite options -> partially computable
   * Only one solution
   *
   * 1. The graph is fully connected.
   * 2. The graph is bi-colored.
   * 3. The color of an edge is random.
   * 4. Th graph needs to have a circle of size 3 were every edge has the same color
   *
   * 1. Generate fully connected graph with k vertices
   * 2. Create every possible coloring
   * 3. If every coloring yields a same-colored-three-circle return Solution
   */

  public static void main(String[] args) {
    measure(() -> tryAllWithXEdges(createMatrixOfSize(9), 21), 1);
  }

  public static void measure(Runnable toBeMeasured, int executionTimes) {
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


  public static boolean tryAllWithXEdges(boolean[][] currGraphMatrix, int edges) {
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
  private static int runExperimentV2(int circleLength) {
    // Number of vertices
    if (CIRCLE_LENGTH < 3) {
      return -1;
    }
    CIRCLE_LENGTH = circleLength;
    boolean[][] graphMatrix = createMatrixOfSize(4);
    for (int k = 3; k <= 15; ++k) {
//        System.out.println("Testing for k= " + graphMatrix.length);
      if (tryEveryColoring(graphMatrix)) {
//          System.out.println("Found solution for k=" + (k + 1));
        return k;
      }
      graphMatrix = createMatrixOfSize(k + 1);
    }
//    System.out.println("No solution found");
    return -1;
  }


  private static boolean[][] createMatrixOfSize(int k) {
    boolean[][] graphMatrix = new boolean[k][k];
    for (int i = 0; i < k; i++) {
      graphMatrix[i][i] = false;
    }
    return graphMatrix;
  }

  private static boolean tryEveryColoring(boolean[][] currGraphMatrix) {
//    System.out.println("Trying for " + currGraphMatrix.length + " nodes");
    List<Pair<Integer, Integer>> toBeMapped = new ArrayList<>();
    for (int i = 0; i < currGraphMatrix.length; i++) {
      for (int j = i + 1; j < currGraphMatrix.length; j++) {
        toBeMapped.add(new Pair<>(i, j));
      }
    }

    BinarySequentialSearch<Pair<Integer, Integer>> search = new BinarySequentialSearch<>(toBeMapped);
    boolean[] possibleColoring = new boolean[toBeMapped.size()];
    long triedColoring = 0;

    while (search.hasNext()) {
      var possSol = search.tryNext(possibleColoring);
      if (!isValidSolution(possSol)) {
        continue;
      }
      triedColoring++;
      var coloredGraph = colorGraph(currGraphMatrix, possSol, toBeMapped);
      // test circle if false also try inverted graph -> resembles other color
      if (containsNoCircle(coloredGraph)) {
        triedColoring++;
        if (containsNoCircle(invertedGraph(coloredGraph))) {
          triedColoring++;
          System.out.println("Tried " + triedColoring + " colorings before false.");
          System.out.println(Arrays.deepToString(coloredGraph));
          return false;
        }
      }

    }

    System.out.println("Tried " + triedColoring + " all true.");
    return true;
  }

  private static boolean isValidSolution(boolean[] possibleSolution) {
    int count = 0;
    for (int i = 0; i < possibleSolution.length; i++) {
      count += possibleSolution[0] ? 1 : 0;
    }
    return count <= (possibleSolution.length + 1) / 2;
  }

  private static boolean[][] colorGraph(boolean[][] currGraphMatrix,
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

  private static boolean[][] colorGraph(boolean[][] currGraphMatrix, boolean[] choices,
      List<Pair<Integer, Integer>> pairs) {
    if (choices.length != pairs.size()) {
      throw new AssertionError();
    }
    for (int i = 0; i < pairs.size(); i++) {
      currGraphMatrix[pairs.get(i).getX()][pairs.get(i).getY()] = choices[i];
      currGraphMatrix[pairs.get(i).getY()][pairs.get(i).getX()] = choices[i];
    }
    return currGraphMatrix;
  }

  private static boolean[][] invertedGraph(boolean[][] graphMatrix) {
    boolean[][] inverted = new boolean[graphMatrix.length][graphMatrix.length];
    for (int i = 0; i < graphMatrix.length; i++) {
      for (int j = 0; j < graphMatrix.length; j++) {
        inverted[i][j] = i != j && !graphMatrix[i][j];
      }
    }
    return inverted;
  }

  private static boolean containsNoCircle(boolean[][] graphMatrix) {
    return containsNoCircle(graphMatrix, false);
  }

  private static boolean containsNoCircle(boolean[][] graphMatrix, boolean inverted) {
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

  private static boolean testForCircleRecursive(boolean[][] graphMatrix, List<Integer> path, boolean inverted) {
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
}
