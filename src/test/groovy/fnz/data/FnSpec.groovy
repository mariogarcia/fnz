package fnz.data

import static Fn.bind
import static Fn.fmap
import static Fn.Just
import static Fn.List
import static Fn.Right
import static Fn.wrap
import static Fn.val
import static Fn.Maybe
import static Fn.Failure
import static Fn.Success

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import spock.lang.Specification

@CompileStatic
class FnSpec extends Specification {

    @CompileStatic(TypeCheckingMode.SKIP)
    void 'Fmap'() {
        given: 'a function (a->b) using functional interface coertion'
            Function<String,Integer> fn =
                { String word -> return word.length() } as Function<String,Integer>
        when: 'applying fmap::(a->b) -> fa -> fb'
            Maybe.Just<Integer> result = fmap(Just("hi"), fn)
        then: 'result should be the expected'
            result instanceof Maybe.Just
            result.isPresent()
            val(result) == 2
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void 'Binding'() {
        when: 'Building a nested binding expression'
            Maybe.Just<Integer> result =
                bind(Just(1)) { Integer x ->
                    bind(Just(x + 1)) { Integer y ->
                        Just(y + 1)
                    }
                }
        then: 'Result should be 2 more'
            result instanceof Maybe.Just
            result.isPresent()
            val(result) == 3
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void 'Using bind with a list monad: looks like comprehensions'() {
        given: 'a list monad'
            ListMonad<Integer> numbers = List(1,2,3,4)
        when: 'applying a function to bind'
            ListMonad<Integer> result =
                bind(numbers){ x -> [x, x + 1] as ListMonad }
        then: 'we should get the expected sequence'
            result.typedRef.value == [1,2,2,3,3,4,4,5]
    }

    void 'using maybe method: monadic value'() {
        given: 'a function to increment a given number'
            def inc = { x ->  x + 1 }
        when: 'trying to apply the computation'
            def tryResult =
                val(fmap(Just(value),wrap(inc)))
        then: 'there could be a result or not'
            Maybe(tryResult).isPresent() == isPresent
        and: 'possible final value should be'
            val(Maybe(tryResult)) == finalValue
        where: 'possible values are'
            value | isPresent | finalValue
            null  | false| null
            2     | true | 3
    }

    void 'using maybe method: simple value'() {
        given: 'a function to increment a given number'
            def inc = { x ->  x + 1 }
        when:
            Maybe result = fmap(Maybe(value), inc)
        then: 'possible final value should be'
            result.isPresent() == isPresent
            val(result) == finalValue
        where: 'possible values are'
            value | isPresent | finalValue
            null  | false| null
            2     | true | 3
    }

    void 'using Failure()'() {
        when: 'trying to use a failure to compute anything'
            Try result = fmap(Failure(), { x -> x })
        then: 'we should not be able'
            result instanceof Try.Failure
            val(result) == null
            result.exception instanceof NullPointerException
    }

    void 'using Success()'() {
        when: 'trying to use a success value to compute anything'
            Try result = fmap(Success(1)) { x -> x + 1 }
        then:
            result instanceof Try.Success
            val(result) == 2
    }

}
