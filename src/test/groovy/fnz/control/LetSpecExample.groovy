package fnz.control

import static fnz.data.Fn.Just

import fnz.data.Maybe

class LetSpecExample {

    Maybe<Integer> nestingLets() {
        let(a: 10, b: 20) {
            let(d: 6, e: { d + 1 }) {
                def result = a + b + d + e
                Just(result)
            }
        }
    }

}
