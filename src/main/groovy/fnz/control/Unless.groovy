package fnz.control

import groovy.transform.CompileStatic

/**
 * This control was inspired by the "unless" keyword in Ruby
 *
 * @marioggar
 * @since 0,1
 */
@CompileStatic
class Unless {

    private Closure<?> block

    static Object ret(Closure<?> executionBlock) {
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
        return Unless.ret(executionBlock).unless(condition)
    }

}
