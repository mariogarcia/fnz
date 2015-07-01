// tag::importStaticTry[]
import fnz.Fnz
// end::importStaticTry[]
import spock.lang.Specification

class MaybeExampleSpec extends Specification {

    void 'handling NPE with Maybe'() {
        expect: 'having a null reference'
        // tag::maybeBasic1[]
        assert 1 == Fnz.Maybe(null).or(Fnz.Just(0)).get() + 1
        // end::maybeBasic1[]
    }

    void 'handling NPE with Maybe without import'() {
        expect: 'having a null reference'
        // tag::maybeBasic2[]
        assert 1 == Maybe(null).or(Just(0)).get() + 1
        // end::maybeBasic2[]
    }

    void 'handling NPE with actions'() {
        expect: 'having a null reference'
        // tag::maybeBasic3[]
        assert Maybe(null)
               .or(Just(0))
               .fmap { x -> x + 1 }
               .fmap { y -> y + 1 }
               .get() == 2
        // end::maybeBasic3[]
    }

    void 'handling NPE with actions and Elvis'() {
        expect: 'having a null reference'
        // tag::maybeBasic4[]
        assert (null ?: 0)
            .collect { x -> x + 1 }
            .collect { y -> y + 1 }
            .find() == 2
        // end::maybeBasic4[]
    }

    // tag::maybeBasic5[]
    void 'invoking a method depending on Maybe'() {
        when: 'having a null reference'
        def number = Maybe(null) // == Nothing()
        def result = execute(number)

        then: 'we should get a bad result'
        result == 'bad'
    }

    String execute(fnz.data.Maybe.Nothing nothing) {
        return 'bad'
    }

    String execute(fnz.data.Maybe.Just value) {
        return 'good'
    }
    // end::maybeBasic5[]

}
