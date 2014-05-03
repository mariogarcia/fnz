package fnz.base

import groovy.transform.CompileStatic

@CompileStatic
class Option<T> {

    final T value

    private Option(T value) {
        this.value = value
    }

    T get() {
        return this.value instanceof Option ?
            this.value.get() : this.value
    }

    static Option<T> of(T value) {
        return new Option(value)
    }

    Boolean isPresent() {
        return this.value instanceof Option ?
            this.value.isPresent() : this.value != null
    }

}
