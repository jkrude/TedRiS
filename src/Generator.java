import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


public class Generator<X, Y> {

  final List<Constraint<X, Y>> constraints;
  final Deque<Y> allY;
  Deque<Node> looseEnds;

  public Generator(Deque<X> xs, Deque<Y> ys, final List<Constraint<X, Y>> constraints) {
    looseEnds = new LinkedList<>();
    this.constraints = constraints;
    this.allY = ys;
    // If there is a possible start add a root node
    Map<X, Y> mapping = new HashMap<>();
    final X topX = xs.pollFirst();
    Deque<Y> filteredList = filterYsForX(topX, mapping);
    if (!filteredList.isEmpty()) {
      Node rootNode = new Node(topX, xs, filteredList, mapping);
      looseEnds.add(rootNode);
    }
  }

  public boolean hasNext() {
    return !looseEnds.isEmpty();
  }

  public Optional<Map<X, Y>> findNextSolution() {
    if (looseEnds.size() == 0) {
      throw new NoSuchElementException();
    } else {
      Optional<Map<X, Y>> optSolution;
      do {
        optSolution = searchFromLooseEnd(looseEnds.pollFirst());
      } while (optSolution.isEmpty() && !looseEnds.isEmpty());
      return optSolution;
    }
  }

  private Optional<Map<X, Y>> searchFromLooseEnd(Node looseEnd) {
    final X currX = looseEnd.currX;
    while (!looseEnd.yOptionsForThisX.isEmpty()) {
      // Explore each option for mappings x->y
      final Y selectedY = looseEnd.yOptionsForThisX.pollFirst();
      Map<X, Y> selectedMapping = new HashMap<>(looseEnd.mappings);
      selectedMapping.put(currX, selectedY);
      Optional<Map<X, Y>> optSol = exploreBranch(new ArrayDeque<>(looseEnd.toBeMappedXs),
          selectedMapping);
      // Test if this branch yields a successful solution
      if (optSol.isPresent()) {
        // If there could be more options save them
        if (!looseEnd.yOptionsForThisX.isEmpty()) {
          looseEnds.addFirst(looseEnd);
        }
        return optSol;
      }
    }
    // All branches were tested but none yielded a solution
    return Optional.empty();
  }

  private Optional<Map<X, Y>> exploreBranch(
      Deque<X> toBeMapped, Map<X, Y> currentMapping) {

    while (!toBeMapped.isEmpty()) {
      // Each iteration represents a deeper level in the search-tree
      final X currX = toBeMapped.pollFirst();
      Deque<Y> yOptions = filterYsForX(currX, currentMapping);
      if (yOptions.isEmpty()) {
        // This branch has no solution
        return Optional.empty();
      } else {
        final Y currY = yOptions.pollFirst();
        if (!yOptions.isEmpty()) {
          // There is more than one option for y in x->y
          Node choice = new Node(currX, new ArrayDeque<>(toBeMapped), yOptions,
              new HashMap<>(currentMapping));
          looseEnds.addFirst(choice);
        }
        currentMapping.put(currX, currY);
      }
    }
    // All x were successfully mapped to a y
    return Optional.of(currentMapping);
  }

  private Deque<Y> filterYsForX(X x, Map<X, Y> currentMapping) {
    return allY.stream()
        .filter(y -> constraints.stream().allMatch(c -> c.test(x, y, currentMapping)))
        .collect(Collectors.toCollection(ArrayDeque::new));
  }

  // A node represents options mapping the current x to a y
  public class Node {

    final X currX; // The current x
    Deque<Y> yOptionsForThisX; // The options for the current x
    Deque<X> toBeMappedXs; // The rest of xs that need a mapping
    Map<X, Y> mappings; // The mappings already done

    public Node(X currX, Deque<X> toBeMappedXs, Deque<Y> yOptionsForThisX, Map<X, Y> mappings) {
      assert toBeMappedXs.size() + mappings.size() == 7;
      this.currX = currX;
      this.toBeMappedXs = toBeMappedXs;
      this.yOptionsForThisX = yOptionsForThisX;
      this.mappings = mappings;
    }
  }
}