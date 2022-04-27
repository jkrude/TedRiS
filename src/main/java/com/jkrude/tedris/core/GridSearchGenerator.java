package com.jkrude.tedris.core;

import com.jkrude.tedris.util.CyclicIterator;
import com.jkrude.tedris.util.LazyGenerator;
import com.jkrude.tedris.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class GridSearchGenerator<T> {

    private final List<BiPredicate<T, T>> constraints;
    private LazyGenerator<T> lazyGenerator;
    private CyclicIterator<T> cyclicIt;

    public GridSearchGenerator(LazyGenerator<T> lazyGenerator, CyclicIterator<T> it,
        List<BiPredicate<T, T>> constraints) {

        this.lazyGenerator = lazyGenerator;
        this.cyclicIt = it;
        this.constraints = constraints;
    }

    public Optional<Pair<T, T>> testNext() {
        T t1 = lazyGenerator.next();
        for (T y : cyclicIt.oneCycle()) {
            if (testConstraints(t1, y)) {
                return Optional.of(new Pair<>(t1, y));
            }
        }
        return Optional.empty();
    }

    private boolean testConstraints(T t1, T y) {
        return constraints.stream().allMatch(pred -> pred.test(t1, y));
    }
}
