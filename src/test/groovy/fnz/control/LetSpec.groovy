package fnz.control

import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class LetSpec extends AstBaseSpec {

    def exampleInstance

    def setup() {
        exampleInstance =
            getClassToTestForPhase(
                FnzAst,
                CompilePhase.CONVERSION).newInstance()
    }

    def 'simple let'() {
        when: 'Initializing expression and executing closure'
            Integer result = exampleInstance.simpleLetExample()
        then: 'The value should be the expected'
            result == 6
    }

    def 'nested let'() {
        when: 'Initializing expression and executing closure'
            Integer result = exampleInstance.nestedLetExample()
        then: 'The value should be the expected'
            result == 60
    }

    def 'computed values'() {
        when: 'Initializing expression and executing closure'
            Integer result = exampleInstance.computedValues()
        then: 'The value should be the expected'
            result == 6
    }

    def 'resolve variables'() {
        when: 'Initializing expression and executing closure'
        Integer result = exampleInstance.sumAndInc(1,2)
        then: 'The value should be the expected'
            result == 4
    }

}
