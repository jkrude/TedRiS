package Core;

import Core.Generator.SearchTreeGenerator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Solver<X, Y> {

  SearchTreeGenerator<X, Y> searchTreeGenerator;

  public Solver(Collection<X> xs, Collection<Y> ys, List<Constraint<X, Y>> constraintList) {
    searchTreeGenerator = new SearchTreeGenerator<>(new HashSet<>(xs), new ArrayDeque<>(ys),
        constraintList);
  }


  public Optional<Map<X, Y>> findOne() {
    Optional<Map<X, Y>> optSolution = Optional.empty();
    while (optSolution.isEmpty() && searchTreeGenerator.hasNext()) {
      optSolution = searchTreeGenerator.testNext();
    }
    return optSolution;
  }

  public List<Map<X, Y>> findAll() {
    List<Map<X, Y>> solutions = new ArrayList<>();
    while (searchTreeGenerator.hasNext()) {
      findOne().ifPresent(solutions::add);
    }
    return solutions;
  }

}
