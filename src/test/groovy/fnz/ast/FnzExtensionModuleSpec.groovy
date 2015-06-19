package fnz.ast

import fnz.data.Try
import fnz.data.Function
import fnz.data.Maybe
import spock.lang.Specification

class FnzExtensionModuleSpec extends Specification {

    void 'testing Left'() {
        when: 'executing an operation over a Left'
            def left = Left(2).fmap { x -> x + 1 }
        then: 'the same value'
            val(left) == 2
    }

    void 'testing Right'() {
        when: 'executing an operation over a Left'
            def right = Right(2).fmap { x -> x + 1 }
        then: 'performs the operation'
            val(right) == 3
    }

    void 'testing Either'() {
        when: 'executing an operation over an Either'
             def source = Either(isLeft ? null : 0)
             def result = source.fmap { x -> x + 1 }
        then: 'performs the operation'
             val(result) == expectedValue
        where:
           isLeft | expectedValue
           true   | null
           false  | 1
    }

    void 'testing Nothing()'() {
        when: 'applying any action over nothing'
            def result = Nothing().fmap { it * 10 }
        then: 'results in nothing'
            result instanceof fnz.data.Maybe.Nothing
            val(result) == null
    }

    void 'test Maybe(A)'() {
        when: 'executing an operation over an Maybe'
             def source = Maybe(isNothing ? null : 0)
             def result = source.fmap { x -> x + 1 }
        then: 'performs the operation'
             val(result) == expectedValue
        where:
           isNothing | expectedValue
           true      | null
           false     | 1
    }

    void 'test Maybe(Monad)'() {
        when: 'executing an operation over an Maybe'
             def source = Maybe(Just(isNothing ? null : 0))
             def result = source.fmap { x -> x + 1 }
        then: 'performs the operation'
             val(result) == expectedValue
        where:
           isNothing | expectedValue
           true      | null
           false     | 1
    }

    void 'test Success'() {
        when: 'using a successful value and applying some operations'
            def result =
                Success(10)
                .fmap { x -> x + 1 }
                .fmap { x -> x * 2 }
        then: 'we should get the result of all of them'
            val(result) == 22
    }

    void 'test Failure (I)'() {
        when: 'using Failure() and applying some operations'
            def result =
                Failure()
                .fmap { x -> x + 1 }
                .fmap { x -> x * 2 }
        then: 'we should get nothing'
            val(result) == null
    }

    void 'test Failure (II)'() {
        when: 'using Failure() and applying some operations'
            def result =
                Success(10)
                .fmap { x -> throw new IllegalArgumentException('not valid') }
        then: 'we should get the value tried'
            val(result) == 10
        and: 'we can always get the produced exception'
            result.exception instanceof IllegalArgumentException
            result.exception.message == 'not valid'
    }

    void 'test List(varargs)'() {
        when: 'creating a list of 4 elements'
            def list = List(1, 2, 3, 4)
        then: 'we should get a list of 4 elements'
            val(list).size() == 4
    }

    void 'test List(iterable)'() {
        when: 'creating a list from another iterable'
            def list = List([1, 2, 3, 4])
        then: 'we should get the expected number of elements'
            val(list).size() == 4
    }

    void 'test wrap(fn)'() {
        when: 'wrapping an unsafe function'
            def unsafe = { 0 / 0 }
            def safe   = wrap(unsafe)
            def result = safe.apply(1)
        then: 'we should get an error wrapped in a Try'
            result instanceof fnz.data.Try.Failure
            val(result) == 1
    }

    void 'test bind'() {
        when:
            def result = bind(Just(1)) { Success(it + 1) }
        then:
            result instanceof Try.Success
            val(result) == 2
    }

    void 'test fmap'() {
        when:
             def result = fmap(Just(1)) { it + 1 }
        then:
            result instanceof Maybe.Just
            val(result) == 2
    }

    void 'test fapply'() {
        when:
            def result = fapply(Just(1), Success({ x -> x + 1 } as Function ))
        then:
            result instanceof Maybe.Just
            val(result) == 2
    }

    void 'recovering from possible failures'() {
        when: 'having a function with a possible error and its alternative'
            def safeFn = recover(
                { x -> x / 0 },
                { y -> y + 1 }
            )
        then: 'we should success on getting a valid value'
            val(Success(1).bind(safeFn)) == 2
    }

    void 'using Failure with a value'() {
        when: 'building a failure based on a given value'
            Try.Failure<Integer> failureValue = Failure(1)
        then: 'we should be able to get the value producing the failure'
            failureValue.get() == 1
        and: 'get an IllegalArgumentException'
            failureValue.exception instanceof IllegalArgumentException
            failureValue.exception.message == failureValue.get().toString()
    }

}
