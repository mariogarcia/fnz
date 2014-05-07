package fnz.control

import static fnz.control.Where.check
import fnz.base.Option

import spock.lang.Unroll
import spock.lang.Specification

class WhereSpecificatione extends Specification {

    @Unroll
    void 'Build a where expression'() {
        given: 'An expression'
            // tag::simpleWhere[]
            Option<String> result = check(weight: value) {
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
            // end::simpleWhere[]
        expect: "The underweight output"
            result.isPresent() == true
            result.get() == expected
        where:
            value | expected
            45    | "You're underweight"
            70    | "You're normal"
            85    | "You're fat"
    }

    @Unroll
    void 'Build a pattern matcher'() {
         given: 'An expression'
            // tag::wherePatternMatching[]
            Option<String> result = check(value) {
                when String then { stringMessage }
                when Integer then { bigIntegerMessage }
                otherwise { otherwiseMessage }
                where {
                    stringMessage     = "Is a String"
                    bigIntegerMessage = "Is a Integer"
                    otherwiseMessage  = "No Idea"
                }
            }
            // end::wherePatternMatching[]
        expect: "The underweight output"
            result.isPresent() == true
            result.get() == expected
        where:
            value | expected
            "hi"  | "Is a String"
            70    | "Is a Integer"
            null  | "No Idea"
    }

}
