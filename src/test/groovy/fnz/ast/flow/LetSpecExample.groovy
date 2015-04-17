package fnz.ast.flow

class LetSpecExample {

    // tag::simpleLet[]
    Integer simpleLetExample() {
        let(x: 1, y: 2,  z: 3) {
            x + y + z
        }
    }
    // end::simpleLet[]

    // tag::computedValues[]
    Integer computedValues() {
        let(x: 1, y: 2,  z: { x + y }) {
            x + y + z
        }
    }
    // end::computedValues[]

    // tag::nestedLet[]
    Integer nestedLetExample() {
        let(a: 5, b: 15) {
            let(c: 15, d: { a + 25 }) {
                b + c + d
            }
        }
    }
    // end::nestedLet[]

    // tag::resolveMethodVariables[]
    Integer sumAndInc(Integer x, Integer y) {
        let(a: x, b: y) {
            a + b + 1
        }
    }
    // end::resolveMethodVariables[]

}
