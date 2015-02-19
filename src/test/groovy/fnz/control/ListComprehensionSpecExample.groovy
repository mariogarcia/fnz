package fnz.control

class ListComprehensionSpecExample {

    List<Integer> getSimpleSequence() {
        return [ i | i << 1..10]
    }

}
