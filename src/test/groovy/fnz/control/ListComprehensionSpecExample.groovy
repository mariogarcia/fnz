package fnz.control

class ListComprehensionSpecExample {

    List<Integer> getSimpleSequence() {
        return [ i | i << (1..10)]
    }

    List<List<Integer>> getSimpleTuples() {
        return [ [i, j] | i << (1..2) , j << (2..4) ]
    }

    List<List<Integer>> getDependentGenerators() {
        return [ [i, j] | i << (2..4) , j << (1..i)]
    }

}
