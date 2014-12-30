package fnz.control

import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class LetSpec extends AstBaseSpec {

    // tag::simpleLet[]
    def 'simple let'() {
        given: 'an instance of the sample class'
            def instance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CANONICALIZATION).newInstance()
        when: 'Initializing expression and executing closure'
            Integer result = instance.simpleLetExample()
        then: 'The value should be the expected'
            result == 6
    }
    // end::simpleLet[]

}
