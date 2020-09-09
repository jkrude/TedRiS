import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Solver<X, Y> {

  Generator<X, Y> generator;

  public Solver(Collection<X> xs, Collection<Y> ys, List<Constraint<X, Y>> constraintList) {
    generator = new Generator<>(new HashSet<>(xs), new ArrayDeque<>(ys), constraintList);
  }


  public Optional<Map<X, Y>> findOne() {
    if (generator.hasNext()) {
      return generator.findNextSolution();
    } else {
      return Optional.empty();
    }
  }

  public List<Map<X, Y>> findAll() {
    List<Map<X, Y>> solutions = new ArrayList<>();
    while (generator.hasNext()) {
      generator.findNextSolution().ifPresent(solutions::add);
    }
    return solutions;
  }

}
