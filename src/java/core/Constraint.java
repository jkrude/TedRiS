package core;

import java.util.Map;

public interface Constraint<X, Y> {

  boolean test(X currX, Y choice, Map<X, Y> alreadyMapped);
}
