package fnz.control

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.with
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.inject

import fnz.base.Option
import groovy.transform.CompileStatic

/**
 * This type of expression is inspired in Clojure's let macro
 *
 * @author @marioggar
 * @since 0.1
 */
@CompileStatic
class Let<T> {

    static final Closure<Map<?,?>> aggregator = { Map acc, Map.Entry entry ->
        acc.get(
            entry.key,
            entry.value instanceof Closure ?
                with(acc, (Closure) entry.value) :
                entry.value
        )
        return acc
    }

    static <T> Option<T> let(final Map<?,?> initValues, final Closure<T> execution) {
        return Option.of(with(inject(initValues, [:], aggregator), execution))
    }

}
