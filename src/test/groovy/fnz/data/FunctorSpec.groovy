package fnz.data

// tag::publicapimethods[]
import static Fn.val
import static Fn.fmap
// end::publicapimethods[]

import spock.lang.Specification

/**
 *
 */
class FunctorSpec extends Specification {

    // tag::functorspec1[]
    void 'applying a given function to a functor'() {
        given: 'a function'
            Function<Integer,Integer> plus_3 = { Integer v -> v + 3 }
        and: 'a functor implementation. This time Maybe.Just implements functor'
            Functor<Integer> boxOfFive = Maybe.just(5)
        when: 'applying the function to functor to get another functor'
            Functor<Integer> result = boxOfFive.fmap(plus_3)
        then: 'the result should be the expected'
            result.typedRef.value == 8
    }
    // end::functorspec1[]

    // tag::functorspec3[]
    void 'applying a given function to a functor with public api'() {
        when: 'using fmap function to execute the fmap method from Just'
            Functor<Integer> result =
                fmap(Maybe.just(5)) { Integer x ->
                    x + 3
                }
        then: 'executing val() to get Just inner value'
            val(result) == 8
    }
    // end::functorspec3[]

    // tag::groovyfunctormockery[]
    def map(Object o, Closure function) {
        o?.with(function)
    }
    // end::groovyfunctormockery[]

    // tag::groovyfunctor[]
    void 'Groovy light version of fmap'() {
        when: 'using fmap function to execute the fmap method from Just'
            def result = map(input) { Integer x ->
                map(x + 3) { Integer y ->
                    y
                }
            }
        then: 'executing val() to get Just inner value'
            result == expected // 8 and null
        where:
            input    | expected
                5    | 8
                null | null
    }
    // end::groovyfunctor[]

    // tag::collect[]
    void 'Groovy collect is "similar" to fmap'() {
        when: 'Applying a function to collect'
            def result =
                input.collect { Integer x ->
                    x + 1
                }
        then: 'checking'
            result == expected
        where: 'possible messy values'
            input      | expected
                [1, 2] | [2, 3]
                null   | []
    }
    // end::collect[]

    // tag::collectandmap[]
    void 'Groovy light version of fmap and collect combined'() {
        when: 'Making the world a safer place'
            def result =
                input.collect { Integer x ->
                    map(x) { Integer y ->
                        (y + 1) * 2
                    }
                }
        then: 'checking'
            result == expected
        where: 'possible messy values are'
            input         | expected
                [1, 2]    | [4, 6]
                [1, null] | [4, null]
    }
    // end::collectandmap[]

}
