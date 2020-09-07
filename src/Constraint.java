import java.util.Map;

interface Constraint<X, Y> {

  boolean test(X currX, Y selectedY, Map<X, Y> mappings);
}
