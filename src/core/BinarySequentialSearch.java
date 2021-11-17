package core;

import java.util.List;


public class BinarySequentialSearch<X> {

  private final List<X> toBeMapped;
  private final long max;
  private long state;

  public BinarySequentialSearch(List<X> toBeMapped) {
    this(toBeMapped, 0);
  }

  public BinarySequentialSearch(List<X> toBeMapped, long initialSize) {
    this.toBeMapped = toBeMapped;
    this.state = initialSize;
    this.max = (long) Math.pow(2, toBeMapped.size());
//    System.out.println("Search-Space <= " + max);
  }

  /*
   * Instance run(){
   *  map toBeMapped to binary
   *  count state up by one
   *  reconvert binary to List<Pair<X,Y>>
   */
  public boolean hasNext() {
    return this.state < this.max - 1;
  }

  public boolean[] tryNext(boolean[] toBeFilled) {
    if (!hasNext()) {
      throw new IllegalStateException("tryNext was called but no next available");
    }
    this.state++;
    return this.extractFromState(toBeFilled);
  }

  private boolean[] extractFromState(boolean[] toBeFilled) {
    String asBString = Long.toBinaryString(this.state);
    asBString = "0".repeat(this.toBeMapped.size() - asBString.length()) + asBString;
    return foo(asBString, toBeFilled);
  }

  private boolean[] foo(String binaryString, boolean[] toBeFilled) {

    for (int i = 0; i < binaryString.length(); i++) {
      toBeFilled[i] = binaryString.charAt(i) != '0';
    }
    return toBeFilled;
  }
}
