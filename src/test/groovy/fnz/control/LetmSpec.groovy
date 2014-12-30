package fnz.control

import static fnz.data.Fn.val

import fnz.data.Maybe
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class LetmSpec extends AstBaseSpec {

    // tag::simpleLetm[]
    def 'simple letm'() {
        given: 'an instance of the sample class'
            def instance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CANONICALIZATION).newInstance()
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result =
                instance.simpleLetmExpression()
        then: 'There should be a value'
            result.isPresent()
        and: 'The value should be built from the deeper values'
            val(result) == 30
    }
    // end::simpleLetm[]

    // tag::nestedLetm[]
    def 'Nesting lets'() {
        given: 'an instance of the sample class'
            def instance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CANONICALIZATION).newInstance()
        when: 'Initializing expression and executing closure'
            Maybe<Integer> result =
                instance.nestingLetms()
        then: 'There should be a value'
            result.isPresent()
        and: 'The value should be built from the deeper values'
            val(result) == 43
    }
    // end::nestedLetm[]

}
