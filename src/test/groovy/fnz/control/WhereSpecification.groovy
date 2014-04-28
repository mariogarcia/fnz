package fnz.control

import static fnz.control.Where.check
import spock.lang.Specification

class WhereSpecificatione extends Specification {

    void 'Build a where expression'() {
        given: 'An expression'
            def values = [weight:60]
            def result = check(values) {
                when { weight <= underweight } then { "You're underweight" }
                when { weight <= normal }      then { "You're normal" }
                when { weight <= fat }         then { "You're fat" }
                otherwise { "You have a strange composition" }
                where {
                    underweight = 50
                    normal      = 70
                    fat         = 90
                }
            }
        expect: "The underweight output"
            result == "You're underweight"
    }

}
