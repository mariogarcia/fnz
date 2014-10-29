package fnz.data

import static ListMonad.list
import static Fn.bind
import spock.lang.Specification

/**
 *
 */
class ListMonadSpec extends Specification {

    void 'using fmap'() {
        given: 'a list of numbers'
            ListMonad<Integer> fa = list(1,2,3,4)
        when: 'incrementing their value'
            ListMonad<Integer> fb = fa.fmap { it + 1 }
        then: 'result should be the expected'
            fb instanceof ListMonad
            fb.typedRef.value == [2,3,4,5]
    }

    void 'using bind'() {
        given: 'a list of numbers'
            ListMonad<Integer> fa = list(1,2,3,4)
            Function<Integer,ListMonad<Integer>> fn = { Integer i ->
                return list(i + 1)
            }
        when: 'binding with a increment function'
            ListMonad<Integer> result = fa.bind(fn)
        then: 'checking result'
            result.typedRef.value == [2,3,4,5]
    }

    void 'using bind for list comprehensions (I)'() {
        given: 'a list of numbers'
            ListMonad<Integer> fa = list("hi","bye")
        and: 'making bind to look like Haskell bind'
            fa.metaClass.'>>=' = { fn -> delegate.bind(fn) }
        and: 'creating a function containing a list monad'
            def wordAndCount = { String w -> list(w, w.length()) }
        when: 'executing the binding'
            def result = fa.'>>=' wordAndCount
        then: 'we should get the word length and vowels in both words'
            result.typedRef.value == list("hi",2,"bye",3).typedRef.value
    }

    void 'using bind for list comprehensions (II)'() {
        given: 'a list of numbers'
            ListMonad<Integer> fa = list("hi","bye")
        and: 'making bind to look like Haskell bind'
            fa.metaClass.'>>=' = { fn -> delegate.bind(fn) }
        and: 'creating a function containing a list monad'
            def wordAndCount = { String w -> list(w, w.length()) }
        when: 'executing the binding'
            def result = fa.'>>=' wordAndCount
        then: 'we should get the word length and vowels in both words'
            result.typedRef.value == list("hi",2,"bye",3).typedRef.value
    }

    // tag::listmonadvsplaingroovy1[]
    void 'Comparing list monad with plain Groovy (I)'() {
        when: 'collecting number, its double and its half with plain Groovy'
            def result1 =
                (1..3)
                    .collect { x -> [x, x * 2, x.div(2)] }
                    .flatten()
        and: 'with the list monad'
            def result2 =
                list(1..3)
                    .bind { x -> list(x, x * 2, x.div(2)) }
        then: 'all results should give the same result'
            result1 == [1,2,0.5,2,4,1,3,6,1.5]
            result1 == result2.typedRef.value
    }
    // end::listmonadvsplaingroovy1[]

}

