package core;

import java.util.List;


public class BinarySequentialSearch<X> {

  private final List<X> toBeMapped;
  private final long max;
  private long state;

  public BinarySequentialSearch(List<X> toBeMapped) {
    this(toBeMapped, 0);
  }

  public BinarySequentialSearch(List<X> toBeMapped, long initialState) {
    this(toBeMapped, initialState, (long) Math.pow(2, toBeMapped.size()));
  }

  public BinarySequentialSearch(List<X> toBeMapped, long initialState, long max) {
    this.toBeMapped = toBeMapped;
    this.state = initialState;
    this.max = max;
//    System.out.println("Search-Space <= " + max);
  }

  /*
   * Instance run(){
   *  map toBeMapped to binary
   *  count state up by one
   *  reconvert binary to List<Pair<X,Y>>
   */
  public boolean hasNext() {
    return this.state < this.max;
  }

  public boolean[] tryNext(boolean[] toBeFilled) {
    if (!hasNext()) {
      throw new IllegalStateException("tryNext was called but no next available");
    }
    var result = this.extractFromState(toBeFilled);
    this.state++;
    return result;
  }

  private boolean[] extractFromState(boolean[] toBeFilled) {
    String asBString = Long.toBinaryString(this.state);
    asBString = "0".repeat(this.toBeMapped.size() - asBString.length()) + asBString;
    for (int i = 0; i < asBString.length(); i++) {
      toBeFilled[i] = asBString.charAt(i) != '0';
    }
    return toBeFilled;
  }
}
