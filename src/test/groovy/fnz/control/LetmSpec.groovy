package fnz.control

import fnz.data.Either

import static fnz.data.Fn.val

import fnz.data.Try
import fnz.data.Maybe
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class LetmSpec extends AstBaseSpec {

    def exampleInstance

    def setup() {
        exampleInstance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CONVERSION).newInstance()
    }

    def 'simple letm'() {
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result =
                exampleInstance.simpleLetmExpression()
        then: 'There should be a value'
            result.isPresent()
        and: 'The value should be built from the deeper values'
            val(result) == 30
    }

    def 'Nesting lets'() {
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result =
                exampleInstance.nestingLetms()
        then: 'There should be a value'
            result.isPresent()
        and: 'The value should be built from the deeper values'
            val(result) == 43
    }

    def 'either monad'() {
        when: 'executing a possible dangerous method'
        Either<Integer> result =
            exampleInstance.workingWithOtherMonads(1, 2)
        then:'we can handle exceptions as data'
        result instanceof Either
        and: 'Result is the expected'
        val(result) == 3
    }

}
