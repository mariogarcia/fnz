package fnz.data

import groovy.transform.CompileStatic
import spock.lang.Specification

/**
 *
 */
@CompileStatic
class FunctionSpec extends Specification {

    // tag::function1[]
    void 'Building a simple function'() {
        given: 'a function instance using closures'
            Function<Integer,Integer> plus_3 = { Integer v ->
                v + 3
            } as Function<Integer,Integer>
        when:  'using it'
            Integer result = plus_3.apply(5)
        then: 'the value to be the number plus 3'
            result == 8
    }
    // end::function1[]

    // tag::function2[]
    void 'Composing function instances'() {
        given: 'a function instance using closures'
            def plus_3 = { Integer v -> v + 3 } as Function<Integer,Integer>
            def plus_4 = { Integer v -> v + 4 } as Function<Integer,Integer>
        when:  'using it'
            Integer result = plus_3.apply(plus_4.apply(5))
        then: 'the value to be the number plus 3'
            result == 12
    }
    // end::function2[]
}

