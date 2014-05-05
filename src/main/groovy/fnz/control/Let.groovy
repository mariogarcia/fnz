package fnz.control

import fnz.base.Option

/**
 * This type of expression is inspired in Clojure's let macro
 *
 * @author @marioggar
 * @since 0.1
 */
class Let<T> {

    static <T> Option<T> let(Map initValues, Closure<T> execution) {
        return Option.of(initValues.with(execution))
    }

}
