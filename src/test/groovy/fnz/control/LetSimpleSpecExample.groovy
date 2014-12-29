package fnz.control

import fnz.data.Maybe
import static fnz.data.Fn.Just

/**
 * Created by mario on 12/28/14.
 */
class LetSimpleSpecExample {

    Maybe<Integer> simpleLetExpression() {
        return let(a: 10, b: 20, c: { a + b }) {
            Just(c)
        }
    }

}
