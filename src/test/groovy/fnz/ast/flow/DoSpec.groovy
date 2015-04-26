package fnz.ast.flow

import fnz.ast.FnzAst

import fnz.test.AstBaseSpec
import org.codehaus.groovy.control.CompilePhase

class DoSpec extends AstBaseSpec {

    def exampleInstance

    void 'checking parameters'() {
        when: 'trying to use $do with wrong arguments'
        def helper = getScriptParser(FnzAst, CompilePhase.CONVERSION)
            helper.parse(
               '''
               package fnz.ast.flow

               class A {
                    void error() {
                          $do(1)
                    }
               }
               '''
            )
        then: 'it will throw an exception'
            thrown(Exception)
    }

    void 'normal expression with explicit return statement'() {
        when: 'trying to use $do with valid arguments'
        and: 'using an explicit monadic value return'
        def helper = getScriptParser(FnzAst, CompilePhase.CONVERSION)
        def exampleClass =
            helper.parse(
               '''
               package fnz.ast.flow

               import fnz.data.Maybe

               class A {
                    Integer returningDoWithoutReturn() {
                          Maybe<Integer> result =
                              $do {
                                  x = Just(1)
                                  y = Just(x + 1)
                                  z = { Just(y + 1) }

                                  return Just(z + 1)
                              }

                         return val(result)
                    }
               }
               '''
            )
        then: 'the result should be the expected'
            exampleClass.newInstance().returningDoWithoutReturn() == 4
    }

    void 'normal expression with $return'() {
        when: 'trying to use $do with valid arguments'
        and: 'using an implicit monadic value return'
        def helper = getScriptParser(FnzAst, CompilePhase.CONVERSION)
        def exampleClass =
            helper.parse(
               '''
               package fnz.ast.flow

               // tag::simpleExample[]
               import fnz.data.Maybe

               class A {
                    Integer returningDoWithReturn() {
                          Maybe<Integer> result =
                              $do {
                                  x = Just(1)
                                  y = Just(x + 1)
                                  z = Just(y)

                                  $return z + 1
                              }

                         return val(result)
                    }
               }
               // end::simpleExample[]
               '''
            )
        then: 'the result should be the expected'
            exampleClass.newInstance().returningDoWithReturn() == 3
    }

    void 'ommiting not assignment expressions'() {
        when: 'trying to use $do with valid arguments'
        and: 'using an implicit monadic value return'
        def helper = getScriptParser(FnzAst, CompilePhase.CONVERSION)
        def exampleClass =
            helper.parse(
               '''
               package fnz.ast.flow

               import fnz.data.Maybe

               class A {
                    Integer returningDoWithReturn() {
                          Maybe<Integer> result =
                              $do {
                                  x = Just(1)
                                  y = Just(x + 1)
                                  z = { Just(y + 1) }

                                  // other binary expressions are ommited
                                  p << 1

                                  $return z + 1
                              }

                         return val(result)
                    }
               }
               '''
            )
        then: 'the result should be the expected'
            exampleClass.newInstance().returningDoWithReturn() == 4
    }

    void 'using underscore to use non returning values expressions'() {
        when: 'trying to use $do with valid arguments'
        and: 'using an implicit monadic value return'
        def helper = getScriptParser(FnzAst, CompilePhase.CONVERSION)
        def exampleClass =
            helper.parse(
               '''
               package fnz.ast.flow

               // tag::wildcard1[]
               import fnz.data.Maybe

               class A {

                    Maybe.Just PrintStrLn(Object val) {
                        println val
                        return Just(val)
                    }

                    Integer returningDoWithReturn() {
                          Maybe<Integer> result =
                              $do {
                                  x = Just(1)
                                  _ = PrintStrLn("x: $x")
                                  y = Just(x + 1)
                                  _ = PrintStrLn("y: $y")
                                  z = { Just(y + 1) }

                                  $return z + 1
                              }

                         return val(result)
                    }
               }
               // end::wildcard1[]
               '''
            )
        then: 'the result should be the expected'
            exampleClass.newInstance().returningDoWithReturn() == 4
    }

}
