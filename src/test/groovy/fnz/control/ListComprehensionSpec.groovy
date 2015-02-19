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
        List<Integer> result = exampleInstance.getSimpleSequence()
        then: 'There should be a value'
        result
        and: 'The sequence should have all expected values'
        result == [1,2,3,4,5,6,7,8,9,10]
    }
}
