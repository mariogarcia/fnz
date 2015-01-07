package fnz.control

import static fnz.data.Fn.val
import static fnz.data.Fn.Right

import fnz.data.Try
import fnz.data.Maybe
import fnz.data.Either
import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class TypeSpec extends AstBaseSpec {

    def helper

    def setup() {
        helper  =
            getScriptParser(
                FnzAst,
                CompilePhase.CONVERSION
            )
    }

    void 'simple type alias'() {
        given: 'a simple inner type example with no generics'
        def exampleClass =
            helper.parse(
               """
               package fnz.samples.type

               class A {
                    static {
                        ftype Fn >= String >> Integer
                    }

                    boolean simpleFunctionalInterface() {
                        Integer result = executeFunction('1') { String x ->
                             x.toInteger()
                        }

                        return (result == 1)
                    }

                    Integer executeFunction(String number, Fn fn) {
                        return fn.apply(number)
                    }

               }
               """
            )
        expect: 'the method to return true'
        exampleClass.newInstance().simpleFunctionalInterface()
    }

    void 'simple type alias: check imported classes'() {
        given: 'a simple inner type example with no generics'
        def exampleClass =
            helper.parse(
               """
               package fnz.samples.type

               import fnz.data.Fn
               import fnz.data.Maybe

               class A {

                    static {
                        ftype Fx >= String >> Maybe
                    }

                    Maybe executeFunction(String number, Fx fx) {
                         return fx.apply(number)
                    }

                    boolean simpleFunctionalInterface() {
                         Maybe<Integer> result = executeFunction('1') { String x ->
                              Fn.Just(Integer.parseInt(x))
                         }

                         Fn.val(result) == 1
                    }

               }
               """
            )
        expect: 'the method to return true'
        exampleClass.newInstance().simpleFunctionalInterface()

    }



}
