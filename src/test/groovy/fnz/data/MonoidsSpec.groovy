package fnz.data

import static groovyx.gpars.GParsPool.withPool
import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import spock.lang.Specification

class MonoidsSpec extends Specification {

    // tag::monoid1[]
    @Immutable // <1>
    @EqualsAndHashCode // <2>
    class V {
        int x, y

        // <3>
        def plus(final V v) {
            return new V (x: x + v.x, y: y + v.y)
        }
    }
    // end::monoid1[]

    // tag::monoid2[]
    void 'simple monoid: associativity'() {
        given: 'three different entries'
            def a = new V(x: 1, y: 2)
            def b = new V(x: 1, y: 3)
            def c = new V(x: 1, y: 4)
        expect: 'applying binaryOp'
            ((a + b) + c) == (a + (b + c))
    }
    // end::monoid2[]

    // tag::monoid3[]
    void 'simple monoid: identity'() {
        given: 'three different entries'
            def a = new V(x: 1, y: 200)
            def e = new V(x: 0, y: 0)
        expect: 'applying binaryOp'
            (e + a) == (a + e)
            (e + a) == a
            (a + e) == a
    }
    // end::monoid3[]

    // tag::monoid4[]
    void 'simple monoid: parallelism'() {
        given: 'three different entries'
            def a = new V(x: 1, y: 2)
            def b = new V(x: 1, y: 3)
            def c = new V(x: 1, y: 4)
        when: 'applying binaryOp in order'
            def result1 = [a, b, c].sum() // <1>
        and: 'applying in any order'
            def result2 = withPool { [a,b,c].sumParallel() } // <2>
        then: 'both results should match'
            result1 == result2
    }
    // end::monoid4[]

}
