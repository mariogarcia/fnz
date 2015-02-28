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

    List<Integer> getClosureGeneratedValues() {
        return [ { i + j } | i << (1..2), j << (2..4)]
    }

    List<Integer> getSimpleNestedListResult() {
        return [ i | i << [ j | j << (1..10)]]
    }

    List<List<Integer>> getComplexNestedListResult() {
        return [
            [ i , j ] | i << [ x | x << [1]],
                        j << [ { y + z } | y << [3,5,7],
                                           z << [1]]
        ]
    }

    Integer yieldInteger() {
        return 1
    }

    List<Integer> getMethodCallReturningOneValue() {
        return [ i | i < [yieldInteger()] ]
    }

    List<Integer> yieldNumbers() {
        return [1,2,3,4,5,6,7,8,9,10]
    }

    List<Integer> getMethodCallReturningACollection() {
        return [ x | x < yieldNumbers() ]
    }

}
