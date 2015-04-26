package fnz.data

import static fnz.Fnz.List
import static fnz.Fnz.Just
import static fnz.Fnz.bind
import static fnz.Fnz.fmap
import static fnz.Fnz.fapply
import static fnz.Fnz.val
import static fnz.Fnz.wrap
import static fnz.Fnz.recover
import static fnz.Fnz.Success

import spock.lang.Unroll
import spock.lang.Specification

class TrySpec extends Specification {

    // tag::basic1[]
    def 'parsing a non valid number: Try'() {
        when: 'trying to parse a non valid number'
            Function unwrapped = { x -> x + 1 }
            Function wrappedfn = wrap { v -> Integer.parseInt(v) } // <1>
            Try result1 = bind(Just('2a'), wrappedfn) // <2>
            Try result2 = bind(result1, unwrapped)
        then: 'computation failed'
            result1.isFailure()
        and: 'any possible composition will return the same failure'
            result2.isFailure()
    }
    // end::basic1[]

    def 'using fapply over a valid value'() {
        given: 'a non safe function'
            Function toTry = { Success(it) }
            Function notUseThis = { Integer x -> x / 0 }
        when: 'trying to apply it to a given value'
            Try result = fapply(bind(Just(2), toTry), Just(notUseThis))
        then: 'we should not be worried about exceptions'
            result.isFailure()
    }

    def 'using fapply over a failure'() {
        given: 'a non safe function'
            Function notUseThis = { Integer x -> x / 0 }
        when: 'trying to apply it to a given value'
            Try result =
                fapply(
                    Try.failure(new Type(2), new Exception("not enough")),
                    Just(notUseThis))
        then: 'we should not be worried about exceptions'
            result.isFailure()
    }

    // tag::exception1[]
    def 'throwing an exception'() {
        when: 'wrapping something that "could" fail :P'
            Function failure = wrap { 0.div(0) }
            // <1>
            Try<Integer> result = bind(Just(1), failure)

            assert result.exception instanceof ArithmeticException
        and: 'wants to propagate the exception'
            result.throwException() // <2>
        then:'the exception will be thrown as usual'
            thrown(ArithmeticException)
    }
    // end::exception1[]

    // tag::recover[]
    def 'using recover()'() {
        when: 'you cant always get what you want'
            def someFnChain =
                recover (
                    { 0.div(0) } as Function, // WRONG
                    recover(
                        { new Date() + "1" } as Function,
                        { throw new IllegalArgumentException("Something bad") } as Function,
                        { 0 } as Function
                    ) // WORST
                )
            def anything = bind(Just(1), someFnChain)
        then: 'you might find you get what you need :P'
            val(val(anything)) == 0 // TODO recover(Function... nFunctions)
    }
    // end::recover[]

    def 'using recover() with no recovering functions'() {
        when:
            Function failingFn = recover({ throw new IllegalArgumentException("ahhh") } as Function)
            def anything = bind(Just(1), failingFn)
        then:
            anything.isFailure()
    }

    @SuppressWarnings('ReturnNullFromCatchBlock')
    def 'classic try catch example'() {
        given: 'a list of numbers as strings'
            def numbers = ["1", "2a", "11", "24", "4A"]
        when:
            def average =
                numbers.findResults { n ->
                    // <1>
                    try {
                        return Integer.parseInt(n)
                    } catch (e) {
                        return null // <2>
                    }
                }.with { list ->
                    list.sum().div(list.size())
                }
        then: 'the average should be 12'
            average == 12
    }

    def 'classic try catch example RELOADED'() {
        given: 'a list of numbers as strings'
            def numbers = ["1", "2a", "11", "24", "4A"]
            def parse = { item -> Integer.parseInt(item) } as Function
            def ZERO = { 0 } as Function
            def addToList = { x -> x ? List(x) : List() }
            def AVG = { list -> list.sum().div(list.size()) }
        when: 'calculating average safely'
            def average =
                val(fmap(
                    Just(
                        numbers.collectMany { n ->
                            val(
                                bind(
                                    bind(Just(n), recover(parse, ZERO)),
                                    addToList
                                )
                            )
                        }
                    ),
                    AVG
                ))
        then: 'the average should be 12'
            average == 12
    }

    def 'classic try catch example MONADIC'() {
        given: 'a list of numbers as strings'
            def numbers = ["1", "2a", "11", "24", "4A"]
            def ZERO = { 0 } as Function
            def AVG = { list -> list.sum().div(list.size()) }
            def parse = { item -> Integer.parseInt(item) } as Function
            def addToList = { x -> x ? List(x) : List() }
        when: 'trying to get the average'
            def numberList  =
                 bind(List(numbers)) { n ->
                     bind(bind(Just(n), recover(parse, ZERO)), addToList)
                 }
            assert numberList instanceof ListMonad
            assert val(numberList).size() == 3
            def average = val(fmap(Just(val(numberList)), AVG))
        then: 'the average should be 12'
            average == 12
    }

    def 'basic execution of a try'() {
        given: 'an action'
            def koDivision = { 0.div(0) }
            def okDivision = { 1.div(2) }
            def addOne = { x -> x + 1 }
        when: 'trying to execute it'
            def failure = bind(bind(Just(1), wrap(koDivision)), wrap(addOne)) // it failed
            def success = bind(bind(Just(1), wrap(okDivision)), wrap(addOne)) // it succeed
        then: 'checking both results'
            failure instanceof Try.Failure
            success instanceof Try.Success
            success.isSuccess()
            !success.isFailure()
            failure.isFailure()
            !failure.isSuccess()
        and: 'success action ends with a given value'
            val(success) == 1.5
    }

    def 'once we have a success we want to make it fail'() {
        given: 'an action'
            def wordLength = { String word -> word.length() }
            def multiplyByTwo = { x -> x * 2 }
            def divByZero = { x -> x.div(0) }
        when: 'we use it wisely'
            Try successSoFar =
                bind(
                    Just("John"),
                    wrap(wordLength >> multiplyByTwo))
        and: 'checking so far so good'
            assert successSoFar.isSuccess()
            assert val(successSoFar) == 8
        and: 'then screw it'
            Try failure = fmap(successSoFar, divByZero)
        then: 'the try instance will return failure'
            failure.isFailure()
            !failure.isSuccess()
    }

    def 'making a failure to throw an exception'() {
        when: 'we use it wisely'
            Function action = { 0.div(0) }
            Try successSoFar =
                fmap(bind(Just(1), wrap(action))) { undefined ->
                    undefined + 1 // wont be executed
                }
        and: 'once we know it ended wrong'
            successSoFar.isFailure()
            !successSoFar.isSuccess()
        and: 'asking the failure to throw an exception'
            successSoFar.throwException()
        then: 'and only then we will get the exception'
            thrown(ArithmeticException)
    }

    def 'using the OR with an alternative VALUE'() {
        given: 'two functions'
            Function BAD = { it / 0 }
            Function GOOD =  { it / 2 }
        and: 'a monadic value'
            Try<Integer> VALUE  = Success(42)
        when: 'trying to execute several functions'
            Try<Integer> resultRight = fmap(VALUE, BAD) | fmap(VALUE, GOOD)
            Try<Integer> resultLeft = fmap(VALUE, GOOD) | fmap(VALUE, BAD)
        then: 'we should be getting the one succeeding'
            val(resultRight) == 21
            val(resultLeft) == 21
            val(resultRight) == val(resultLeft)
    }

    def 'using the OR with an alternative FUNCTION'() {
        given: 'two functions'
            Function BAD = wrap { it / 0 }
            Function GOOD =  wrap { 2 }
        and: 'a monadic value'
            Try<Integer> VALUE  = Success(42)
        when: 'trying to execute several functions'
            Try<Integer> resultRight = bind(VALUE, BAD) | GOOD
            Try<Integer> resultLeft = bind(VALUE, GOOD) | BAD
        then: 'we should be getting the one succeeding'
            val(resultRight) == 2
            val(resultLeft) == 2
            val(resultRight) == val(resultLeft)
    }

    def 'building a try with no chance of recovery'() {
        when:
            Try notRecoverable =
                fmap(Try.failure(new Exception('not found'))) { File file -> file.text }
        then:
            notRecoverable.isFailure()
            !notRecoverable.isSuccess()
        and:
            notRecoverable.getTypedRef()
            !notRecoverable.getTypedRef().getValue()
            !val(notRecoverable)
    }

    // tag::tryFunction1[]
    @Unroll
    def 'check Try with a value and a function'() {
        when: 'executing an unsafe action'
            Try result   = Try(value) { x -> 1 / x }
            Try computed = result.fmap { y -> y + 1 }
        then: 'asking if it is a failure or a success'
            computed.isSuccess() == isSuccess
            computed.isFailure() == !isSuccess
        and: 'getting value'
            val(computed) == resultExpected
        where: 'possible values are'
            value | isSuccess | resultExpected
              1   |   true   |       2
              0   |   false  |       0
    }
    // end::tryFunction1[]


    // tag::tryFunction2[]
    @Unroll
    def 'check Try only with a function'() {
        when: 'executing an unsafe action'
            Try result = Try { value / 0 } // <1>
        then: 'asking if it is a failure or a success'
            result.isSuccess() == isSuccess
            result.isFailure() == !isSuccess
        where: 'possible values are'
            value | isSuccess
              1   |   false
              0   |   false
    }
    // end::tryFunction2[]

    @Unroll
    void 'testing bind with type awareness and unit'() {
        when: 'using a valid expression'
            TypeAwareFunction<Integer,Try<Integer>> fn = { clazz, value ->
                return clazz.unit(value + 1)
            }
            Try<Integer> possible = option.bind2(fn)
        then: 'the result should have the type'
            possible instanceof Try
        and: 'we should get the expected value'
            val(possible) == expected
        where: 'possible options are'
            option                                         | expected
            Try.success(1)                                 | 2
            Try.failure(new IllegalArgumentException('a')) | null
    }

}
