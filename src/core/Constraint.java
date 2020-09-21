package core;

import java.util.Map;

public interface Constraint<X, Y> {

  boolean test(X currX, Y selectedY, Map<X, Y> mappings);
}
