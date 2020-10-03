package entwicklerheld.scaleChallengeHard;

import core.SearchTree;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;


class Scale {

    public static void main(String[] args) {
        // poor test cases emulating the official tests.
        List<List<Integer>> sol = getMasses(2, Arrays.asList(1, 3, 9, 27, 81, 243));
        assert sol.get(0).equals(List.of(1));
        assert sol.get(1).equals(List.of(3));

        sol = getMasses(346, Arrays.asList(1, 3, 9, 27, 81, 243));
        System.out.println(sol);
    }

    static List<List<Integer>> getMasses(Integer slowLorisWeight, List<Integer> allMasses) {
        // You need to implement this method.
        // You can also add attributes to the class and add new methods or functions
        Objects.requireNonNull(slowLorisWeight);
        allMasses.forEach(Objects::requireNonNull);
        if (total(allMasses) < slowLorisWeight || allMasses.isEmpty()) {
            throw new IllegalArgumentException();
        }
        allMasses.sort(Comparator.reverseOrder()/*(x, y) -> {
            if (x.equals(y)) {
                return 0;
            }
            return Math.abs(slowLorisWeight - x) < Math.abs(slowLorisWeight - y) ? -1 : 1;
        }*/);
        Queue<Integer> allMassesQueue = new ArrayDeque<>(allMasses);
        List<Entry<Integer, Choice>> alreadyMapped = new ArrayList<>();
        alreadyMapped.add(new SimpleEntry<>(slowLorisWeight, Choice.Left));
        SearchTree<Integer, Choice> searchTree = new SearchTree<>(new ChoicesForMasses(),
            Scale::validSolution, allMassesQueue, alreadyMapped);
        Optional<List<Entry<Integer, Choice>>> optSol = searchTree.testUntilFound();
        if (optSol.isPresent()) {
            List<List<Integer>> solution = interpretMapping(optSol.get());
            solution.get(0).remove(slowLorisWeight);
            return solution;
        } else {
            // This method is only supposed to be called if a solution exists.
            throw new RuntimeException("No solution found");
        }
    }

    private static boolean validSolution(List<Entry<Integer, Choice>> alreadyMapped) {
        List<List<Integer>> leftRight = interpretMapping(alreadyMapped);
        return total(leftRight.get(0)) == total(leftRight.get(1));
    }

    private static List<List<Integer>> interpretMapping(
        List<Entry<Integer, Choice>> alreadyMapped) {
        List<Integer> leftSide = new ArrayList<>();
        List<Integer> rightSide = new ArrayList<>();
        alreadyMapped.forEach(entry -> {

            switch (entry.getValue()) {
                case Left:
                    leftSide.add(entry.getKey());
                    break;
                case Right:
                    rightSide.add(entry.getKey());
                    break;
            }
        });
        return Arrays.asList(leftSide, rightSide);
    }

    private static int total(List<Integer> list) {
        return list.stream().reduce(0, Integer::sum);
    }


    enum Choice {
        Right, Left, Discard
    }

    static class ChoicesForMasses implements core.ChoosingStrategy<Integer, Choice> {

        @Override
        public Queue<Choice> sortedOptionsForY(
            Integer curr,
            List<Entry<Integer, Choice>> alreadyMapped,
            Queue<Integer> toBeMapped) {

            Deque<Choice> options = new ArrayDeque<>();

            List<List<Integer>> interMapping = interpretMapping(alreadyMapped);
            int leftSum = total(interMapping.get(0));
            int rightSum = total(interMapping.get(1));
            int leftOverSum = total(new ArrayList<>(toBeMapped));
            if (leftSum == rightSum) {
                return new ArrayDeque<>(Collections.singleton(Choice.Discard));
            } else {
                if ((leftSum > rightSum) && ((rightSum + curr) <= (leftSum + leftOverSum))) {
                    options.addFirst(Choice.Right);
                } else if ((leftSum + curr) <= (rightSum + leftOverSum)) {
                    options.addLast(Choice.Left);
                }
                if (leftSum + leftOverSum >= rightSum) {
                    options.addLast(Choice.Discard);
                }
            }
            return options;
        }
    }

}
