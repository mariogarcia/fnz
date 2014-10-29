package fnz.data

// tag::imports[]
import static Fn.val
import static Maybe.just
import static Maybe.nothing
// end::imports[]
import spock.lang.Specification

/**
 *
 */
class MaybeSpec extends Specification {

    // tag::fapplyspec[]
    void 'Applicative: Maybe applicative implementation'() {
        when: 'combining two closures'
            def inc = { Integer v -> v + 1 }
            def byTwo = { Integer v -> v * 2 }
        and: 'using the combination as a Function'
            def combination = (inc >> byTwo) as Function
        then: 'if the value is nothing the function shouldnt be applied'
            val(nothing().fapply(just(combination))) == null // <1>
        and: 'otherwise if the initial value is correct the function will work'
            val(just(1).fapply(just(combination))) == 4 // <2>
    }
    // end::fapplyspec[]

    // tag::maybebind[]
    void 'Monad: using maybe to shortcircuit a process'() {
        given: 'a function dividing only even numbers'
            def half = { BigDecimal possible ->
                return possible.intValue() % 2 == 0 ?
                    Maybe.just(possible.div(2)) :
                    Maybe.nothing()
            }
        and: 'another function multiplying by three'
            def threeTimes = { BigDecimal possible ->
                return Maybe.just(possible * 3)
            }
        when: 'starting the process'
            Maybe<Integer> result =
                Maybe.just(sampleNumber)
                    .bind(half) // <1>
                    .bind(half) // <2>
                    .bind(threeTimes) // <3>
        then: 'checking the result'
            val(result) == expected
        where: 'sample numbers and expectations are'
            sampleNumber | expected
                100      |    75
                200      |   150
                 50      |   null
    }
    // end::maybebind[]

    // tag::maybeor1[]
    void 'testing maybe alternatives (I)'() {
        when: 'something has no value and adding an alternative'
            Maybe<String> name = nothing().or(just("john"))
        then: 'we should get the alternative'
            val(name) == 'john'
    }
    // end::maybeor1[]

    void 'testing maybe alternatives (II)'() {
        when: 'something has value and adding an alternative'
            Maybe<String> name = just("mario").or(just("john"))
        then: 'we should get first value'
            val(name) == 'mario'
    }

    // tag::maybeor2[]
    void 'testing maybe alternatives (III)'() {
        when: 'something has value and adding an alternative'
            Maybe<Integer> nameLength =
                just("mario")
                    .bind { nothing() } // some fancy logic here
                    .or(just(0))
        then: 'we should get first value'
            val(nameLength) == 0
    }
    // end::maybeor2[]

    void 'testing maybe is present'() {
        when: 'something has value and adding an alternative'
            Maybe<String> name = just("mario")
            Maybe<String> city = nothing()
        then: 'we should get first value'
            name.isPresent() == true
            city.isPresent() == false
    }

}

