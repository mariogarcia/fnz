package fnz.control

import static fnz.data.Fn.Just
import static fnz.data.Fn.Nothing

import fnz.data.Maybe
import groovy.transform.CompileStatic

/**
 * This control was inspired by the "unless" keyword in Ruby
 *
 * @author @marioggar
 * @since 0,1
 */
@CompileStatic
class Unless<T> {

    private final Closure<T> block

    private Unless(Closure<T> block) {
       this.block = block
    }

    Maybe<T> unless(Boolean condition) {
        return !condition ? Just(block.call()) : Nothing()
    }

    static <T> Maybe<T> unless(Boolean condition, Closure<Maybe<T>> executionBlock) {
        return new Unless(executionBlock).unless(condition)
    }

}
