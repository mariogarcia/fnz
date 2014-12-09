package fnz.control

import static fnz.data.Fn.val

import fnz.data.Maybe
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class LetSpec extends AstBaseSpec {

    def 'Simple let expression'() {
        given: 'an instance of the sample class'
            def instance =
                getClassToTestForPhase(
                    FnzAst,
                    CompilePhase.CANONICALIZATION).newInstance()
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result = instance.simpleLetExpression()
        then: 'There should return a value'
            result.isPresent() == true
        and: 'the value should be...'
            val(result) == 30
    }

    // tag::nestedLet[]
    def 'Nesting lets'() {
        given: 'an instance of the sample class'
            def instance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CANONICALIZATION).newInstance()
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result =
                instance.nestingLets()
        then: 'There should be a value'
            result.isPresent()
        and: 'The value should be built from the deeper values'
            val(result) == 40
    }
    // end::nestedLet[]

}
