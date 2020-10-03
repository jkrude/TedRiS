package teded;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import util.Pair;

public class CoinRiddle {

    //https://www.youtube.com/watch?v=pnSw8g3DPHw&list=PLJicmE8fK0EiFRt1Hm5a_7SJFaikIFW30&index=22

    /*
     * Grid Search
     * Infinite options
     * Problem parameters not fully known
     * expected one solution
     */

    private static Optional<Pair<Number, Number>> solve() {
        for (int i = 0; i <= 20; i++) {
            for (int j = 0; j <= i; ++j) {
                if (new SolutionCheck().test(i, j)) {
                    return Optional.of(new Pair<>(i, j));
                }
            }
        }
        return Optional.empty();
    }

    private static List<Pair<Number, Number>> solveAll() {
        List<Pair<Number, Number>> allSolutions = new ArrayList<>();
        for (int i = 0; i <= 1000; i++) {
            for (int j = 0; j <= i; ++j) {
                if (new SolutionCheck().test(i, j)) {
                    allSolutions.add(new Pair<>(i, j));
                }
            }
        }
        return allSolutions;
    }

    public static void main(String[] args) {
        var solutionList = CoinRiddle.solveAll();
        if (solutionList.isEmpty()) {
            System.out.println("No solution found");
        } else {
            for (var solution : solutionList) {
                System.out.println("Solution found: " + solution.getX() + ", " + solution.getY());
            }
        }
    }

    public static class SolutionCheck implements BiPredicate<Integer, Integer> {

        @Override
        public boolean test(Integer coinsOnNewPile, Integer coinsFlipped) {
            Objects.requireNonNull(coinsFlipped);
            Objects.requireNonNull(coinsOnNewPile);
            // Test all free parameters: number of silver coins picked, how many of those are flipped
            // In each case the number of silver coins in both piles is known and need to be equal

            int pickedSilverMax = Math.min(20, coinsOnNewPile);
            for (int pickedSilver = 0; pickedSilver <= pickedSilverMax; ++pickedSilver) {
                int flippedSilverMax = Math.min(pickedSilver, coinsFlipped);
                // If the number of coinsFlipped and pickedSilver is greater than the new pile,
                // at least some silver coins are flipped.
                int flippedSilverMin = coinsFlipped + pickedSilver - coinsOnNewPile;
                for (int flippedSilver = flippedSilverMin; flippedSilver <= flippedSilverMax;
                    ++flippedSilver) {
                    if (pickedSilver - flippedSilver + coinsFlipped - flippedSilver !=
                        20 - pickedSilver) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
