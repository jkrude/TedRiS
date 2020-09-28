package util;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class LazyGenerator<X> {

    protected X initialState;
    protected X state;
    protected Function<X, X> next;

    public LazyGenerator(X initialState, Function<X, X> next) {
        this.initialState = initialState;
        this.state = initialState;
        this.next = next;
    }

    public X next() {
        state = next.apply(state);
        return state;
    }

    public void reset() {
        state = initialState;
    }
}


class LazyFinalGenerator<X> extends LazyGenerator<X> {

    private Function<X, Boolean> reachedMax;

    public LazyFinalGenerator(X initialState, Function<X, X> next,
        Function<X, Boolean> reachedMax) {
        super(initialState, next);
        this.reachedMax = reachedMax;
    }

    public boolean hasNext() {
        return !reachedMax.apply(state);
    }

    @Override
    public X next() {
        if (hasNext()) {
            return super.next();
        } else {
            throw new NoSuchElementException();
        }
    }
}