package entwicklerheld.scaleChallengeHard;

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

    static Queue<BranchOption> availableBranches = new ArrayDeque<>();
    static ChoosingStrategy strategy = new ChoosingStrategy();

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
        List<Entry<Integer, Choice>> alreadyMapped = new ArrayList<>();
        alreadyMapped.add(new SimpleEntry<>(slowLorisWeight, Choice.Left));
        Queue<Integer> toBeMapped = new ArrayDeque<>(allMasses);
        Integer firstMass = toBeMapped.poll();
        assert firstMass != null;
        Queue<Choice> optionsForFirstMapping = strategy
            .sortedOptionsForY(firstMass, alreadyMapped, toBeMapped,
                Arrays.asList(Choice.values()));
        for (Choice option : optionsForFirstMapping) {
            availableBranches.add(new BranchOption(firstMass, option, alreadyMapped, toBeMapped));
        }
        Optional<List<Entry<Integer, Choice>>> optSol = checkAllBranches();
        if (optSol.isPresent()) {
            List<List<Integer>> solution = interpretMapping(optSol.get());
            solution.get(0).remove(slowLorisWeight);
            return solution;
        } else {
            throw new RuntimeException("No solution found");
        }
    }


    private static Optional<List<Entry<Integer, Choice>>> traverseBranch(
        List<Entry<Integer, Choice>> alreadyMapped,
        Queue<Integer> toBeMapped) {

        while (!toBeMapped.isEmpty()) {
            if (validSolution(alreadyMapped)) {
                return Optional.of(alreadyMapped);
            }
            Integer curr = toBeMapped.poll();
            assert curr != null;
            Queue<Choice> options = strategy.sortedOptionsForY(
                curr, alreadyMapped, toBeMapped, Arrays.asList(Choice.values()));
            if (options.isEmpty()) {
                return Optional.empty();
            }
            Choice bestChoice = options.poll();
            if (!options.isEmpty()) {
                options.forEach(option -> availableBranches.add(
                    new BranchOption(curr, option, alreadyMapped, toBeMapped)));
            }
            alreadyMapped.add(new SimpleEntry<>(curr, bestChoice));
        }
        return validSolution(alreadyMapped) ? Optional.of(alreadyMapped) : Optional.empty();
    }

    private static Optional<List<Entry<Integer, Choice>>> checkAllBranches() {
        Optional<List<Entry<Integer, Choice>>> optSol = Optional.empty();
        while (!availableBranches.isEmpty() && optSol.isEmpty()) {
            BranchOption b = availableBranches.poll();
            assert b != null;
            optSol = traverseBranch(b.alreadyMapped, b.toBeMapped);
        }
        return optSol;
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
        List<List<Integer>> leftRight = new ArrayList<>();
        leftRight.add(leftSide);
        leftRight.add(rightSide);
        return leftRight;
    }

    private static int total(List<Integer> list) {
        return list.stream().reduce(0, Integer::sum);
    }


    enum Choice {
        Right, Left, Discard
    }

    static class ChoosingStrategy {

        public Queue<Choice> sortedOptionsForY(Integer curr,
            List<Entry<Integer, Choice>> alreadyMapped,
            Queue<Integer> toBeMapped, List<Choice> allPossibleYs) {

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

    public static class BranchOption {

        List<Entry<Integer, Choice>> alreadyMapped;
        Queue<Integer> toBeMapped;

        public BranchOption(final int curr, final Choice choice,
            final List<Entry<Integer, Choice>> alreadyMapped, final Queue<Integer> toBeMapped) {
            this.alreadyMapped = new ArrayList<>(alreadyMapped);
            this.toBeMapped = new ArrayDeque<>(toBeMapped);
            this.alreadyMapped.add(new SimpleEntry<>(curr, choice));
        }
    }

}
