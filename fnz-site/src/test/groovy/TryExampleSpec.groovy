// tag::importStaticTry[]
import static fnz.Fnz.Try
// end::importStaticTry[]
import spock.lang.Specification

class TryExampleSpec extends Specification {

    // tag::tryBasic1[]
    void 'get result from an unsafe operation'() {
        when: 'trying to apply a value to an unsafe operation'
            // 'two' and 'four' will throw an Exception!
            def numbers = ['1', 'two', '3', 'four'}
               .collect { String n -> Try(n) { it.toInteger() }  }
               .findAll() // Using Groovy Truth

        then: 'we should get the successful values'
            numbers*.get() == [1, 3] // Failure results have been filtered

    }
    // end::tryBasic1[]

    void 'get result from an unsafe operation'() {
        when: 'trying to apply a value to an unsafe operation'
            def numbers = ['1', 'two', '3', 'four'}
                // tag::tryFunction[]
               .collect { String n -> Try(n, Integer.&parseInt)
                // end::tryFunction[]
               .findAll() // Success and Failure know about Groovy truth

        then: 'we should get the successful values'
            numbers*.get() == [1, 3] // Failure results have been filtered

    }
    // end::tryBasic1[]

}
