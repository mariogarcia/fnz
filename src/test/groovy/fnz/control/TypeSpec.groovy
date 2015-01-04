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
        def helper  =
            getScriptParser(
                FnzAst,
                CompilePhase.CONVERSION
            )

        exampleInstance =
            helper.parse(
               """
               class A {
                    static {
                        ftype Fn >= String >> Integer
                    }

                    boolean simpleFunctionalInterface() {
                        Fn function = this.&some as Fn
                        Boolean result = function.apply('1') == 1

                        return result
                    }

                    Integer some(String x) {
                        return 1
                    }

               }
               """
            ).newInstance()
    }

    def 'simple type alias'() {
        expect:
        exampleInstance.simpleFunctionalInterface()
    }

}
