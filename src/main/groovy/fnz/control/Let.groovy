package fnz.control

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.inject

import fnz.base.Option

/**
 * This type of expression is inspired in Clojure's let macro
 *
 * @author @marioggar
 * @since 0.1
 */
class Let<T> {

    static <T> Option<T> let(Map initValues, Closure<T> execution) {

        Closure<Map<?,?>> aggregator = { Map acc, Map.Entry entry ->
            acc.get(
                entry.key,
                entry.value instanceof Closure ?
                    acc.with(entry.value) :
                    entry.value
            )
            return acc
        }

        Map<?,?> evaluatedInitValues =
            inject(initValues, [:], aggregator)


        return Option.of(evaluatedInitValues.with(execution))
    }

}
