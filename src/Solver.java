import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Solver<X, Y> {

  Generator<X, Y> generator;

  public Solver(Collection<X> xs, Collection<Y> ys, List<Constraint<X, Y>> constraintList) {
    generator = new Generator<>(new ArrayDeque<>(xs), new ArrayDeque<>(ys), constraintList);
  }

  public Solver(List<X> xs, Collection<Y> ys, List<Constraint<X, Y>> constraintList,
      Comparator<X> sortingXs) {
    xs.sort(sortingXs);
    Deque<X> orderedXs = new ArrayDeque<>(xs);
    generator = new Generator<>(orderedXs, new ArrayDeque<>(ys), constraintList);
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
