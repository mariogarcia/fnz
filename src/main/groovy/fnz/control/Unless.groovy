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

    private final Closure<T> block

    private Unless(Closure<T> block) {
       this.block = block
    }

    Option<T> unless(Boolean condition) {
        if (!condition) {
            return Option.of(block.call())
        }

        return Option.of(null)
    }

    static <T> Option<T> unless(Boolean condition, Closure<Option<T>> executionBlock) {
        return new Unless(executionBlock).unless(condition)
    }

}
