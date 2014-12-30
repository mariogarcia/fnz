package fnz.control

class LetSpecExample {

    Integer simpleLetExample() {
        let(x: 1, y: 2,  z: { x + y }) {
            x + y + z
        }
    }

    Integer nestedLetExample() {
        let(a: 5, b: 15) {
            let(c: 15, d: { a + 25 }) {
                b + c + d
            }
        }
    }

}
