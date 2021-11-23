package teded.TimeTravel;

import static teded.TimeTravel.TimeTravelRiddle.colorGraph;
import static teded.TimeTravel.TimeTravelRiddle.createMatrixOfSize;
import static teded.TimeTravel.TimeTravelRiddle.invertedGraph;
import static teded.TimeTravel.TimeTravelRiddle.toBeMapped;

import core.BinarySequentialSearch;
import java.util.ArrayList;
import java.util.List;
import teded.TimeTravel.TimeTravelRiddle.SearchCallback;
import util.Pair;


class SearchThread extends Thread {

  private final int Cycle_LENGTH;
  private final boolean[][] graph;
  private final int initialState;
  private final int max;
  private final SearchCallback callback;

  public SearchThread(int cycleLength, int numNode, int initialState, int max,
      SearchCallback callback) {
    this.Cycle_LENGTH = cycleLength;
    this.max = max;
    this.graph = createMatrixOfSize(numNode);
    this.initialState = initialState;
    this.callback = callback;
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
        if (hasNoCircle(invertedGraph(coloredGraph), false)) {
          triedColoring++;
          this.callback.reportResult(graph, triedColoring);
          return;
        }
      }
    }
    if (!isInterrupted()) {
      this.callback.reportResult(triedColoring);
    }
  }

  private boolean hasNoCircle(boolean[][] graphMatrix, boolean inverted) {
    for (int i = 0; i < graphMatrix.length; i++) {
      for (int j = 0; j < graphMatrix.length; j++) {
        if (graphMatrix[i][j] ^ inverted) {
          List<Integer> path = new ArrayList<>(this.Cycle_LENGTH);
          path.add(i);
          path.add(j);
          boolean result = hasCircleRec(graphMatrix, path, inverted);
          if (result) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean hasCircleRec(boolean[][] graphMatrix, List<Integer> path, boolean inverted) {

    int lastVisitedNode = path.get(path.size() - 1);
    if (path.size() == this.Cycle_LENGTH) {
      return graphMatrix[lastVisitedNode][path.get(0)];
    }

    for (int i = 0; i < graphMatrix.length; ++i) {
      if ((graphMatrix[lastVisitedNode][i] ^ inverted) && !path.contains(i)) {
        List<Integer> nextPath = new ArrayList<>(path);
        nextPath.add(i);
        var result = hasCircleRec(graphMatrix, nextPath, inverted);
        if (result) {
          return true; // otherwise continue with next i
        }
      }
    }
    return false;
  }

}
