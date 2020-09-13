package Core.Generator;

import java.util.Map;
import java.util.Optional;

public interface Generator<X, Y> {

    boolean hasNext();

    Optional<Map<X, Y>> testNext();
}
