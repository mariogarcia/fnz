package fnz.control

import fnz.data.Maybe
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

import static fnz.data.Fn.val

/**
 * Created by mario on 12/28/14.
 */
class LetSimpleSpec extends AstBaseSpec {

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

}
