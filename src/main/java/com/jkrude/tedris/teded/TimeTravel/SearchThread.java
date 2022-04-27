package com.jkrude.tedris.teded.TimeTravel;

import static com.jkrude.tedris.teded.TimeTravel.TimeTravelRiddle.colorGraph;
import static com.jkrude.tedris.teded.TimeTravel.TimeTravelRiddle.createMatrixOfSize;
import static com.jkrude.tedris.teded.TimeTravel.TimeTravelRiddle.toBeMapped;

import com.jkrude.tedris.core.BinarySequentialSearch;
import com.jkrude.tedris.teded.TimeTravel.TimeTravelRiddle.SearchCallback;
import com.jkrude.tedris.util.Pair;
import java.util.List;


class SearchThread extends Thread {

  private final int CYCLE_LENGTH;
  private final boolean[][] graph;
  private final long initialState;
  private final long max;
  private final SearchCallback callback;

  public SearchThread(int cycleLength, int numNode, long initialState, long max,
      SearchCallback callback) {
    this.CYCLE_LENGTH = cycleLength;
    this.max = max;
    this.graph = createMatrixOfSize(numNode);
    this.initialState = initialState;
    this.callback = callback;
  }

  private static boolean contains(int[] arr, int searched) {
    for (int i : arr) {
      if (i == searched) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void run() {
    List<Pair<Integer, Integer>> toBeMapped = toBeMapped(this.graph.length);

    BinarySequentialSearch<Pair<Integer, Integer>> search = new BinarySequentialSearch<>(toBeMapped, initialState, max);
    boolean[] possibleColoring = new boolean[toBeMapped.size()];
    long triedColoring = 0;

    while (search.hasNext() && !isInterrupted()) {
      var possSol = search.tryNext(possibleColoring);
      triedColoring++;
      var coloredGraph = colorGraph(this.graph, possSol, toBeMapped);
      // test circle if false also try inverted graph -> resembles other color
      if (hasNoCircle(coloredGraph, false)) {
        triedColoring++;
        if (hasNoCircle(coloredGraph, true)) {
          this.callback.reportResult(graph, triedColoring);
          return;
        }
      }
      if (triedColoring % 5e8 == 0) {
        System.out.println("Checked " + triedColoring + " possibilities");
      }
    }
    if (!isInterrupted()) {
      this.callback.reportResult(triedColoring);
    }
  }

  private boolean hasNoCircle(boolean[][] graphMatrix, boolean inverted) {
    for (int i = 0; i < graphMatrix.length; i++) {
      for (int j = 0; j < graphMatrix.length; j++) {
        if (i != j && (graphMatrix[i][j] ^ inverted)) {
          int[] path = new int[this.CYCLE_LENGTH];
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
    if (size == this.CYCLE_LENGTH) {
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
