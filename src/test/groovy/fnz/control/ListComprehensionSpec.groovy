package fnz.control

import static fnz.data.Fn.val
import static fnz.data.Fn.Right

import fnz.data.Try
import fnz.data.Maybe
import fnz.data.Either
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class ListComprehensionSpec extends AstBaseSpec {

    def exampleInstance

    def setup() {
        exampleInstance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CONVERSION).newInstance()
    }

    def 'simple sequence'() {
        when: 'Initializing expression and executing closure'
        List<Integer> result = exampleInstance.simpleSequence
        then: 'There should be a value'
        result
        and: 'The sequence should have all expected values'
        result == [1,2,3,4,5,6,7,8,9,10]
    }

    def 'simple tuple'() {
        when: 'using two iterables'
        List<List<Integer>> listOfTuples = exampleInstance.simpleTuples
        then: 'checking the values'
        listOfTuples == [
            [1,2],[1,3],[1,4],
            [2,2],[2,3],[2,4]
        ]
    }

    def 'dependent generators'() {
        when: 'one generator depends on values of another'
        List<List<Integer>> listOfTuples = exampleInstance.dependentGenerators
        then: 'we should get the proper progression'
        listOfTuples == [
            [2,1],[2,2],
            [3,1],[3,2],[3,3],
            [4,1],[4,2],[4,3],[4,4]
        ]
    }

    def 'getting values from a closure expression'() {
        when: 'one generator creates values using a closure'
        List<Integer> listOfTuples = exampleInstance.closureGeneratedValues
        then: 'we should get the proper progression'
        listOfTuples == [3,4,5,4,5,6]
    }

    def 'getting values from nested lists'() {
        when: 'having nested list comprehensions'
        List<Integer> result = exampleInstance.getSimpleNestedListResult()
        then:'the result should be the expected'
        result == [1,2,3,4,5,6,7,8,9,10]
    }

    def 'getting values from different types of nested lists'() {
        when: 'having nested list comprehensions'
        List<List<Integer>> result = exampleInstance.getComplexNestedListResult()
        then: 'checking the result'
        result == [
            [1,4],
            [1,6],
            [1,8]
        ]

    }

}
