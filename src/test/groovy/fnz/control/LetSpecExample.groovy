package fnz.control

class LetSpecExample {

    Integer simpleLetExample() {
        let(x: 1, y: 2,  z: { x + y }) {
            x + y + z
        }
    }

}
