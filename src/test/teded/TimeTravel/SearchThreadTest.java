package teded.TimeTravel;

import core.BinarySequentialSearch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import teded.TimeTravel.TimeTravelRiddle.SearchCallback;

public class SearchThreadTest {

  static final int NODES = 5;
  static final int CYCLE_LENGTH = 3;
  boolean[][] graphCycle;
  boolean[][] graphNoCycle;
  int noCycleEnc;
  int cycleEnc;

  public static void main(String[] args) {
    SearchThreadTest test = new SearchThreadTest();
    test.setUp();
    test.run();
  }

  @Before
  public void setUp() {
    graphNoCycle = new boolean[][]{
        {false, true, false, false, true},
        {true, false, true, false, false},
        {false, true, false, true, false},
        {false, false, true, false, true},
        {true, false, false, true, false}};
    ;
    graphCycle = new boolean[5][5];
    for (int i = 0; i < graphNoCycle.length; i++) {
      graphCycle[i] = graphNoCycle[i].clone();
    }
    graphCycle[2][3] = false;
    graphCycle[3][2] = false;

    noCycleEnc = simulateEncoding(graphNoCycle);
    cycleEnc = simulateEncoding(graphCycle);

    assertPremise(graphCycle, cycleEnc);
    assertPremise(graphNoCycle, noCycleEnc);


  }

  private int simulateEncoding(boolean[][] graph) {
    return TimeTravelRiddle.toBeMapped(graph.length).stream()
        .map(pair -> graph[pair.getX()][pair.getY()])
        .map(b -> b ? "1" : "0")
        .reduce((s1, s2) -> s1 + s2)
        .map(bString -> Integer.parseInt(bString, 2))
        .orElseThrow();
  }

  private void assertPremise(boolean[][] graph, int enc) {
    var shouldToBeMapped = TimeTravelRiddle.toBeMapped(graph.length);
    var search = new BinarySequentialSearch<>(shouldToBeMapped, enc, enc + 1);
    var result = search.tryNext(new boolean[shouldToBeMapped.size()]);
    for (int i = 0; i < result.length; i++) {
      boolean expected = graph[shouldToBeMapped.get(i).getX()][shouldToBeMapped.get(i).getY()];
      Assert.assertEquals(expected, result[i]);
    }
  }

  @Test
  public void run() {
    // Graph without cycle
    Assert.assertFalse(threadFoundOnlyCycle(this.noCycleEnc, this.noCycleEnc + 1));
    // Graph with cycle
    Assert.assertTrue(threadFoundOnlyCycle(this.cycleEnc, this.cycleEnc + 1));
  }

  void printEdges(boolean[][] graph) {
    // Use https://csacademy.com/app/graph_editor/ for a quick overview
    TimeTravelRiddle.toBeMapped(graph.length).stream()
        .filter(pair -> graph[pair.getX()][pair.getY()])
        .forEach(pair -> System.out.println(pair.getX() + " " + pair.getY()));
  }

  boolean threadFoundOnlyCycle(int start, int end) {
    SearchThread[] threads = new SearchThread[1];
    var callback = new SearchCallback(threads);
    threads[0] = new SearchThread(CYCLE_LENGTH, NODES, start, end, callback);
    threads[0].start();
    try {
      threads[0].join();
    } catch (InterruptedException e) {
      Assert.fail();
    }
    return callback.allGraphsHadCycles();
  }
}