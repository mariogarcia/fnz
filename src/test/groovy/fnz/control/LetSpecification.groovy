package fnz.control

import static fnz.control.Let.let
import static fnz.control.Unless.unless
import static fnz.control.Where.check

import fnz.base.Option
import spock.lang.Unroll
import spock.lang.Specification

class LetSpecification extends Specification {

    def 'Simple let expression'() {
        when: 'Initializing expression and executing closure'
            Option<Integer> result = let(x: 10, y: 20) {
                return x + y
            }
        then: 'There should return a value'
            result.isPresent() == true
        and: 'the value should be...'
            result.get() == 30
    }

    def 'Evaluating let expressions'() {
        when: 'Initializing expression and executing closure'
            Option<Integer> result =
                let(x: { 10 }, y: { 20 }, z: { x + y }) {
                    return z + 1
                }
        then: 'There should return a value'
            result.isPresent() == true
        and: 'the value should be...'
            result.get() == 31
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
            unk | isValid
            5   | false
            4   | true
            0   | true
    }

    def 'Using where within let'() {
        when: 'Having a where clause within a let'
            Option<Integer> result =
                let(x: { xparam }, y: {yparam}, z: { x + y }) {
                    check(measure: z) {
                        when { measure <= MINIMUM } then { MINIMUM }
                        when { measure <= MEDIUM }  then { MEDIUM }
                        when { measure <= MAXIMUM } then { MAXIMUM }
                        where {
                            MINIMUM = 10
                            MEDIUM  = 50
                            MAXIMUM = 100
                        }
                    }
                }
        then: 'The value should be the expected'
            result.get() == expected
        where: 'Possible values are'
            xparam | yparam | expected
            4      | 3      | 10
            20     | 30     | 50
            60     | 30     | 100
    }

}

