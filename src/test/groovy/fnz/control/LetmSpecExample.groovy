package fnz.control

import static fnz.data.Fn.Just

import fnz.data.Maybe

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

}
