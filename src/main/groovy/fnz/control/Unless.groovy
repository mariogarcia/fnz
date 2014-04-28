package fnz.control

import groovy.transform.CompileStatic

/**
 * This control was inspired by the "unless" keyword in Ruby
 *
 * @marioggar
 */
@CompileStatic
class Unless {

    private Closure<?> block

    static Object check(Closure<?> executionBlock) {
        Unless unlessStatement = new Unless()
        unlessStatement.block = executionBlock
        return unlessStatement
    }

    Object unless(Boolean condition) {
        if (!condition) {
            return block()
        }
        return null
    }

    static Object unless(Boolean condition, Closure<?> executionBlock) {
        return Unless.check(executionBlock).unless(condition)
    }

}
