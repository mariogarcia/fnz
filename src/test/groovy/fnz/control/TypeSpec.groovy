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
                // tag::simpleTypeAlias[]
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
               // end::simpleTypeAlias[]
               """
            )
        expect: 'the method to return true'
        exampleClass.newInstance().simpleFunctionalInterface()
    }

    void 'simple type error'() {
        when: 'a simple inner type example with no generics'
        def exampleClass =
            helper.parse(
               """
               package fnz.samples.type

               class A {
                    static {
                        ftype Fn
                    }
               }
               """
            )
        then: 'the method to return true'
            thrown(Exception)
    }

    void 'simple type checking we could work without package'() {
        expect: 'parsing doesnt throw any error'
        def exampleClass =
            helper.parse(
               """
               class A {
                    static {
                        ftype Fn >= String >> Integer
                    }
               }
               """
            )
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

    void 'simple generic type'() {
        given: 'a simple inner type example with one generic type'
        def exampleClass =
            helper.parse(
               """
               // tag::genericsBasicReturnType[]
               package fnz.samples.type

               import fnz.data.Fn
               import fnz.data.Maybe

               import groovy.transform.CompileStatic

               @CompileStatic
               class A {

                    static {
                        ftype Fx(X) >= String >> Maybe(X)
                    }

                    Maybe<Integer> executeFunction(String number, Fx<Integer> fx) {
                         return fx.apply(number)
                    }

                    boolean simpleFunctionalInterface() {
                         Maybe<Integer> result = executeFunction('1') { String x ->
                              Fn.Just(Integer.parseInt(x))
                         }

                         Fn.val(result) == 1
                    }

               }
              // end::genericsBasicReturnType[]
               """
            )
        expect: 'the method to return true'
        exampleClass.newInstance().simpleFunctionalInterface()
    }

    void 'complex generic type 1'() {
        given: 'a simple FI with more than one generic involved'
        def exampleClass =
            helper.parse("""
                // tag::fullGenericsExample[]
                package fnz.samples.type

                class A {
                    static {
                        ftype Fx(X,Y) >= X >> Y
                    }

                    Integer executeFunction(String source, Fx<String,Integer> fx) {
                        fx.apply(source)
                    }

                    String executeAnotherFunction(Integer source, Fx<Integer,String> fx) {
                        fx.apply(source)
                    }

                    boolean complexFunctionalInterface() {
                        Integer result1 = executeFunction('1') { String x ->
                            Integer.parseInt(x)
                        }

                        String result2 = executeAnotherFunction(1) { Integer x ->
                            x.toString()
                        }

                        result1 == 1 && result2 == '1'
                    }
                }
                // end::fullGenericsExample[]
            """)
        expect: 'the method to return true'
        exampleClass.newInstance().complexFunctionalInterface()
    }

        void 'complex generic type 2'() {
        given: 'a simple FI with more than one generic involved'
        def exampleClass =
            helper.parse("""
                // tag::fullGenericsWithType[]

                package fnz.samples.type

                import fnz.data.Fn
                import fnz.data.Maybe

                class A {
                    static {
                        ftype Fx(X,Y) >= Maybe(X) >> Y
                    }

                    Integer executeFunction(String source, Fx<String,Integer> fx) {
                        fx.apply(Fn.Just(source))
                    }

                    boolean complexFunctionalInterface() {
                        Integer result = executeFunction('1') { Maybe<String> x ->
                            Fn.val(x.fmap { Integer.parseInt(it) })
                        }

                        result == 1
                    }
                }
                // end::fullGenericsWithType[]
            """)
        expect: 'the method to return true'
        exampleClass.newInstance().complexFunctionalInterface()
    }

    void 'complex generic type 3'() {
        given: ''
        def exampleClass =
            helper.parse('''
                package fnz.samples.type

                import fnz.data.Fn
                import fnz.data.Maybe

                class A {
                    static {
                        ftype Fx(X,Y,Z) >= [X,Y] >> List(Z)
                    }

                    List<Double> executeFunction(Fx<Integer,Integer,Double> fx) {
                        return fx.apply(1,2)
                    }

                    boolean complexFunctionalInterface() {
                        List<Double> result = executeFunction { x, y ->
                            [x.toDouble(), y.toDouble()]
                        }

                        result instanceof List
                    }
                }
            ''')
        expect: 'the method to return true'
        exampleClass.newInstance().complexFunctionalInterface()
    }

    void 'complex generic type 4'() {
        given: ''
        def exampleClass =
            helper.parse('''
                // tag::multipleParameters[]
                package fnz.samples.type

                import static fnz.data.Fn.*
                import fnz.data.*

                class A {
                    static {
                        ftype Fx(X) >= [Maybe(X),Maybe(X)] >> X
                    }

                    Integer executeFunction(Fx<Integer> fx) {
                        return fx.apply(Just(1), Just(2))
                    }

                    boolean complexFunctionalInterface() {
                        Integer result =
                            executeFunction { Maybe<Integer> a, Maybe<Integer> b ->
                                return val(a) + val(b)
                            }

                        result == 3
                    }
                }
                // end::multipleParameters[]
            ''')
        expect: 'the method to return true'
        exampleClass.newInstance().complexFunctionalInterface()
    }

    void 'complex generic type 5'() {
        given: ''
        def exampleClass =
            helper.parse('''
                package fnz.samples.type

                import static fnz.data.Fn.*
                import fnz.data.*

                class A {
                    static {
                        ftype Fold(X) >= ListMonad(X) >> X
                    }

                    Integer executeFunction(Fold<Integer> fx) {
                        return fx.apply(List(1,2))
                    }

                    boolean complexFunctionalInterface() {
                        Integer result =
                            executeFunction { ListMonad<Integer> list ->
                                val(list).sum()
                            }

                        result == 3
                    }
                }
            ''')
        expect: 'the method to return true'
        exampleClass.newInstance().complexFunctionalInterface()
    }
}
