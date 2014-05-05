package fnz.control

import fnz.base.Option
import groovy.transform.CompileStatic

/**
 * This control was inspired by the "unless" keyword in Ruby
 *
 * @author @marioggar
 * @since 0,1
 */
@CompileStatic
class Unless<T> {

    private Closure<T> block

    static <T> Unless<T> ret(Closure<T> executionBlock) {
        Unless<T> unlessStatement = (Unless<T>)new Unless()
        unlessStatement.block = executionBlock

        return unlessStatement
    }

    Option<T> unless(Boolean condition) {
        if (!condition) {
            return Option.of(block.call())
        }

        return Option.of(null)
    }

    static <T> Option<T> unless(Boolean condition, Closure<Option<T>> executionBlock) {
        return Unless.ret(executionBlock).unless(condition)
    }

}
