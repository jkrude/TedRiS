package com.jkrude.tedris.teded.TimeTravel;


import com.jkrude.tedris.core.BinarySequentialSearch;
import com.jkrude.tedris.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class SearchWorker implements Callable<Optional<Long>> {

  private final long from;
  private final long to;
  private final int cycleLength;
  private final int numNodes;

  public SearchWorker(long from, long to, int cycleLength, int numNodes) {
    this.from = from;
    this.to = to;
    this.cycleLength = cycleLength;
    this.numNodes = numNodes;
  }

  private static boolean contains(int[] arr, int searched) {
    for (int i : arr) {
      if (i == searched) {
        return true;
      }
    }
    return false;
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

  static boolean[][] colorGraph(boolean[][] currGraphMatrix, boolean[] choices,
      List<Pair<Integer, Integer>> pairs) {
    assert choices.length == pairs.size();
    for (int i = 0; i < pairs.size(); i++) {
      currGraphMatrix[pairs.get(i).getX()][pairs.get(i).getY()] = choices[i];
      currGraphMatrix[pairs.get(i).getY()][pairs.get(i).getX()] = choices[i];
    }
    return currGraphMatrix;
  }

  /**
   * @return the graph instance which had no cycle or empty if all had one.
   */
  @Override
  public Optional<Long> call() {
    boolean[][] graph = createMatrixOfSize(numNodes);
    List<Pair<Integer, Integer>> toBeMapped = toBeMapped(graph.length);

    BinarySequentialSearch<Pair<Integer, Integer>> search = new BinarySequentialSearch<>(toBeMapped, this.from,
        this.to);
    boolean[] possibleColoring = new boolean[toBeMapped.size()];
    long triedColoring = 0;

    while (search.hasNext()) {
      var possSol = search.tryNext(possibleColoring);
      triedColoring++;
      var coloredGraph = colorGraph(graph, possSol, toBeMapped);
      // test circle if false also try inverted graph -> resembles other color
      if (hasNoCircle(coloredGraph, false)) {
        triedColoring++;
        if (hasNoCircle(coloredGraph, true)) {
          return Optional.of(search.getState());
        }
        if (triedColoring % ((long) Math.pow(10, 8) * 5) == 0) {
          System.out.println("Checked " + triedColoring + " possibilities");
        }
      }
    }
    return Optional.empty();
  }

  private boolean hasNoCircle(boolean[][] graphMatrix, boolean inverted) {
    for (int i = 0; i < graphMatrix.length; i++) {
      for (int j = 0; j < graphMatrix.length; j++) {
        if (i != j && (graphMatrix[i][j] ^ inverted)) {
          int[] path = new int[this.cycleLength];
          path[0] = i;
          path[1] = j;
          boolean result = hasCircleRec(graphMatrix, path, inverted, 2);
          if (result) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean hasCircleRec(boolean[][] graphMatrix, int[] path, boolean inverted, int size) {

    int lastVisitedNode = path[size - 1];
    if (size == this.cycleLength) {
      return graphMatrix[lastVisitedNode][path[0]] ^ inverted;
    }

    for (int i = 0; i < graphMatrix.length; ++i) {
      if (i != lastVisitedNode && (graphMatrix[lastVisitedNode][i] ^ inverted) && !contains(path, i)) {
        path[size] = i;
        var result = hasCircleRec(graphMatrix, path, inverted, size + 1);
        if (result) {
          return true; // otherwise continue with next i
        }
      }
    }
    return false;
  }

}
