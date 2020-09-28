package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CyclicIterator<X> {

    Iterable<X> baseIt;
    Iterator<X> cyclicIt;

    public CyclicIterator(Iterable<X> baseIt) {
        this.baseIt = baseIt;
    }

    public X next() {
        if (!cyclicIt.hasNext()) {
            cyclicIt = baseIt.iterator();
        }
        return cyclicIt.next();
    }

    public List<X> oneCycle() {
        Iterator<X> tmpIt = baseIt.iterator();
        List<X> oneCycle = new ArrayList<>();
        tmpIt.forEachRemaining(oneCycle::add);
        return oneCycle;
    }
}
