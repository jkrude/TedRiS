package core;

import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

public interface ChoosingStrategy<X, Y> {

  Queue<Y> sortedOptionsForY(
      X curr,
      List<Entry<X, Y>> alreadyMapped,
      Queue<X> toBeMapped);


}
