package fnz.control

import static fnz.data.Fn.Just

import fnz.data.Maybe

class LetSpecExample {

    Maybe<Integer> simpleLetExpression() {
        return let(x: 10, y: 20) { Just(x + y) }
    }

    Maybe<Integer> nestingLets() {
        return let(x: 10, y: 20) {
                def z = x + y
                let(x:6, y: 4) {
                    return Just(x + y + z)
                }
            }
    }

    Maybe<Integer> evaluatingLetExpressions() {
        return let(x: { 10 }, y: { 20 }, z: { x + y }) {
            Just(z + 1)
        }
    }

}
