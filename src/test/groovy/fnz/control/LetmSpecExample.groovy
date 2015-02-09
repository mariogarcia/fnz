package fnz.control

import fnz.data.Try
import fnz.data.Either
import fnz.data.Maybe

import static fnz.data.Fn.Just
import static fnz.data.Fn.Right
import static fnz.data.Fn.Left
import static fnz.data.Fn.wrap
import static fnz.data.Fn.bind

class LetmSpecExample {

    Maybe<Integer> simpleLetmExpression() {
        return letm(a: Just(10), b: Just(20), c: { Just(a + b) }) {
            Just(c)
        }
    }

    Maybe<Integer> nestingLetms() {
        letm(a: Just(10), b: Just(20)) {
            letm(d: Just(6), e: { Just(d + 1) }) {
                def result = a + b + d + e
                Just(result)
            }
        }
    }

    // tag::workingWithOtherMonads[]
    Either<Integer> workingWithOtherMonads(final Integer first, final Integer second) {
        return letm(x: Just(first), y: Just(second)) {
            letm(result: { Just(first + second) }) {
                result == 3 ? Right(result) : Left()
            }
        }
    }
    // end::workingWithOtherMonads[]

    // tag::combineWithTry[]
    Try<Integer> combineWithTry(Integer first, Integer second) {
        letm(x: Just(first), y: Just(second)) {
            bind(Just(second), wrap(this.&dangerousMethod.curry(first)))
        }
    }

    Integer dangerousMethod(Integer x, Integer y) {
        return x / y
    }
    // end::combineWithTry[]

}
