package fnz.control

import static fnz.data.Fn.val
import static fnz.data.Fn.Right

import fnz.data.Try
import fnz.data.Maybe
import fnz.data.Either
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class TypeSpec extends AstBaseSpec {

    def exampleInstance

    def setup() {
        exampleInstance =
                getClassToTestForPhase(
                FnzAst,
                CompilePhase.CONVERSION).newInstance()
    }

    def 'simple type alias'() {
        expect:
        exampleInstance.executeFirstExample()
    }

}
