package fnz.ast.flow

import static fnz.data.Fn.val
import static fnz.data.Fn.Right

import fnz.ast.FnzAst

import fnz.data.Try
import fnz.data.Maybe
import fnz.data.Either
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

    def 'combine with either monad'() {
        when: 'executing'
        Either<Integer> result =
            exampleInstance.workingWithOtherMonads(1, 2)
        then:'we should get the '
        result instanceof Either.Right
        and: 'Result is the expected'
        val(result) == 3
    }

    def 'combine with try monad'() {
        when: 'executing a possible dangerous method'
        Try result = exampleInstance.combineWithTry(a,b)
        then: 'we should get a failure instance'
        result.class.isAssignableFrom(expected)
        where: 'possible values and expectations are'
          a | b | expected
          1|0|Try.Failure
          1|1|Try.Success
    }

}
