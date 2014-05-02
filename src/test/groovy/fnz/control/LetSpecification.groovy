package fnz.control

import static fnz.control.Let.let

import fnz.base.Option
import spock.lang.Unroll
import spock.lang.Specification

class LetSpecification extends Specification {

    def 'Simple let expression'() {
        when:
            Option<Integer> result =
                let(x: 10, y: 20) {
                    return x + y
                }
        then:
            result.isPresent() == true
        and:
            result.get() == 30
    }

}

