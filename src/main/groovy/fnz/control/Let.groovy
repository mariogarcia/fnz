package fnz.control

import fnz.base.Option

class Let<T> {

    static <T> Option<T> let(Map initValues, Closure<T> execution) {
        return Option.of(initValues.with(execution))
    }

}
