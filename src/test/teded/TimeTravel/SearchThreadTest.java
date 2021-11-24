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
  int NoCycleEnc;
  int cycleEnc;

  public static void main(String[] args) {
    SearchThreadTest test = new SearchThreadTest();
    test.setUp();
    test.run();
  }

  @Before
  public void setUp() {
    graphNoCycle = new boolean[NODES][NODES];
    graphCycle = new boolean[NODES][NODES];
    graphNoCycle[0][1] = true;
    graphNoCycle[1][2] = true;
    graphNoCycle[2][3] = true;
    graphNoCycle[3][4] = true;
    graphNoCycle[4][0] = true;
    graphNoCycle[0][2] = false;
    graphNoCycle[0][3] = false;
    graphNoCycle[1][3] = false;
    graphNoCycle[1][4] = false;
    graphNoCycle[2][4] = false;
    graphNoCycle[1][0] = true;
    graphNoCycle[2][1] = true;
    graphNoCycle[3][2] = true;
    graphNoCycle[4][3] = true;
    graphNoCycle[0][4] = true;
    graphNoCycle[2][0] = false;
    graphNoCycle[3][0] = false;
    graphNoCycle[3][1] = false;
    graphNoCycle[4][1] = false;
    graphNoCycle[4][2] = false;
    graphNoCycle[0][0] = false;
    graphNoCycle[1][1] = false;
    graphNoCycle[2][2] = false;
    graphNoCycle[3][3] = false;
    graphNoCycle[4][4] = false;
    for (int i = 0; i < graphNoCycle.length; i++) {
      graphCycle[i] = graphNoCycle[i].clone();
    }
    graphCycle[2][3] = false;
    graphCycle[3][2] = false;

    NoCycleEnc = 613;
    cycleEnc = 609;

    assertPremise(graphCycle, cycleEnc);
    assertPremise(graphNoCycle, NoCycleEnc);

  }

  private void assertPremise(boolean[][] graph, int enc) {
    var shouldToBeMapped = TimeTravelRiddle.toBeMapped(NODES);
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
    Assert.assertFalse(threadFoundOnlyCycle(this.NoCycleEnc, this.NoCycleEnc + 1));
    // Graph with cycle
    Assert.assertTrue(threadFoundOnlyCycle(this.cycleEnc, this.cycleEnc + 1));
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