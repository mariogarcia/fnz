// tag::importStaticTry[]
import fnz.Fnz
// end::importStaticTry[]
import spock.lang.Specification

class MaybeExampleSpec extends Specification {

    // tag::maybeBasic1[]
    void 'handling NPE with Maybe'() {
        when: 'having a null reference'
            Integer value = Fnz.Maybe(null).or(Fnz.Just(0)).get() + 1

        then: 'we dont have to worry about NPE'
            value == 1
    }
    // end::maybeBasic1[]

    // tag::maybeBasic2[]
    void 'handling NPE with Maybe without import'() {
        when: 'having a null reference'
            Integer value = Maybe(null).or(Just(0)).get() + 1

        then: 'we dont have to worry about NPE'
            value == 1
    }
    // end::maybeBasic2[]

}
