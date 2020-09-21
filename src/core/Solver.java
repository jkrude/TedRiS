package core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Solver<X, Y> {

  SearchTree<X, Y> searchTree;

  public Solver(Collection<X> xs, Collection<Y> ys, List<Constraint<X, Y>> constraintList) {
    searchTree = new SearchTree<>(new HashSet<>(xs), new ArrayDeque<>(ys),
        constraintList);
  }


  public Optional<Map<X, Y>> findOne() {
    Optional<Map<X, Y>> optSolution = Optional.empty();
    while (optSolution.isEmpty() && searchTree.hasNext()) {
      optSolution = searchTree.testNext();
    }
    return optSolution;
  }

  public List<Map<X, Y>> findAll() {
    List<Map<X, Y>> solutions = new ArrayList<>();
    while (searchTree.hasNext()) {
      findOne().ifPresent(solutions::add);
    }
    return solutions;
  }

}
