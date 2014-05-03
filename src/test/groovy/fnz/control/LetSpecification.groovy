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

    def 'Using unless within let'() {
        when: "Using less inside a let expression"
            Option<Integer> result =
                let(x: 10, y: 5, z: unk) {
                    unless((x + y + z ) == 20) {
                       return z
                    }
                }
        then: "The option result should be the expected"
            result.isPresent() == isValid
        where: "The possible unknown values are"
            unk|isValid
            5|false
            4|true
            0|true
    }

    def 'Using where within let'() {
        when:
            Option<Integer> result =
                let(x: xparam, y: yparam) {
                    when { x + y <= MINIMUM } then { MINIMUM }
                    when { x + y <= MEDIUM } then { MEDIUM }
                    when { x + y <= MAXIMUM } then { MAXIMUM }
                    where {
                        MINIMUM = 10
                        MEDIUM = 50
                        MAXIMUM = 100
                    }
                }
        then:
            result.get() == expected
        where:
            xparam|yparam|expected
            4|3|10
            20|30|50
            60|30|100
    }

}

