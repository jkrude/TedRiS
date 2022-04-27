package com.jkrude.tedris.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;


public class SearchTree<X, Y> {

  private Queue<BranchOption> availableBranches;
  private ChoosingStrategy<X, Y> strategy;
  private Predicate<List<Entry<X, Y>>> validator;

  private SearchTree(
      ChoosingStrategy<X, Y> strategy,
      Predicate<List<Entry<X, Y>>> solutionValidator) {
    this.strategy = strategy;
    this.availableBranches = new ArrayDeque<>();
    this.validator = solutionValidator;
  }

  public SearchTree(
      ChoosingStrategy<X, Y> strategy,
      Predicate<List<Entry<X, Y>>> solutionValidator,
      Queue<X> toBeMapped,
      List<Entry<X, Y>> alreadyMapped) {
    this(strategy, solutionValidator);
    init(toBeMapped, alreadyMapped);
  }

  public SearchTree(
      ChoosingStrategy<X, Y> strategy,
      Predicate<List<Entry<X, Y>>> solutionValidator,
      Queue<X> toBeMapped) {
    this(strategy, solutionValidator);
    init(toBeMapped);
  }

  private void init(Queue<X> toBeMapped) {
    init(toBeMapped, new ArrayList<>());
  }

  private void init(Queue<X> toBeMapped, List<Entry<X, Y>> alreadyMapped) {
    X first = toBeMapped.poll();
    Queue<Y> optionsForFirstMapping = strategy.sortedOptionsForY(
        first, alreadyMapped, toBeMapped);
    for (Y option : optionsForFirstMapping) {
      availableBranches.add(new BranchOption(first, option, alreadyMapped, toBeMapped));
    }
  }

  public boolean hasNext() {
    return !availableBranches.isEmpty();
  }

  public Optional<List<Entry<X, Y>>> testNext() {
    if (availableBranches.isEmpty()) {
      return Optional.empty();
    } else {
      BranchOption branchOption = availableBranches.poll();
      return traverseBranch(branchOption.alreadyMapped, branchOption.toBeMapped);
    }
  }

  private Optional<List<Entry<X, Y>>> traverseBranch(
      List<Entry<X, Y>> alreadyMapped,
      Queue<X> toBeMapped) {

    while (!toBeMapped.isEmpty()) {
      if (validator.test(alreadyMapped)) {
        return Optional.of(alreadyMapped);
      }
      X curr = toBeMapped.poll();
      assert curr != null;
      Queue<Y> options = strategy.sortedOptionsForY(
          curr, alreadyMapped, toBeMapped);
      if (options.isEmpty()) {
        return Optional.empty();
      }
      Y bestY = options.poll();
      if (!options.isEmpty()) {
        options.forEach(option -> availableBranches.add(
            new BranchOption(curr, option, alreadyMapped, toBeMapped)));
      }
      alreadyMapped.add(new SimpleEntry<>(curr, bestY));
    }
    return validator.test(alreadyMapped) ? Optional.of(alreadyMapped) : Optional.empty();
  }

  public Optional<List<Entry<X, Y>>> testUntilFound() {
    Optional<List<Entry<X, Y>>> optSol = Optional.empty();
    while (!availableBranches.isEmpty() && optSol.isEmpty()) {
      BranchOption b = availableBranches.poll();
      assert b != null;
      optSol = traverseBranch(b.alreadyMapped, b.toBeMapped);
    }
    return optSol;
  }

  public class BranchOption {

    List<Entry<X, Y>> alreadyMapped;
    Queue<X> toBeMapped;

    public BranchOption(final X curr, final Y Y,
        final List<Entry<X, Y>> alreadyMapped, final Queue<X> toBeMapped) {
      this.alreadyMapped = new ArrayList<>(alreadyMapped);
      this.toBeMapped = new ArrayDeque<>(toBeMapped);
      this.alreadyMapped.add(new SimpleEntry<>(curr, Y));
    }
  }

}
