package fnz.base

import groovy.transform.CompileStatic

@CompileStatic
class Option<T> {

    final T value

    private Option(T value) {
        this.value = value
    }

    T get() {
        return value instanceof Option ?  value.get() : value
    }

    static Option<T> of(T value) {
        return new Option(value)
    }

    Boolean isPresent() {
        return value instanceof Option ?  value.isPresent() : value != null
    }

}
