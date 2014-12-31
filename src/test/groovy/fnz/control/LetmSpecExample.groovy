package fnz.control

import fnz.data.Either
import fnz.data.Maybe

import static fnz.data.Fn.Just
import static fnz.data.Fn.Right

class LetmSpecExample {

    Maybe<Integer> simpleLetmExpression() {
        return letm(a: 10, b: 20, c: { a + b }) {
            Just(c)
        }
    }

    Maybe<Integer> nestingLetms() {
        letm(a: 10, b: 20) {
            letm(d: 6, e: { d + 1 }) {
                def result = a + b + d + e
                Just(result)
            }
        }
    }

    Either<Integer> workingWithOtherMonads(final Integer first, final Integer second) {
        return letm(x: first, y: second) {
            Right(x + y)
        }
    }

}
