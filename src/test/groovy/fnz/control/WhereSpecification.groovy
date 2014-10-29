package fnz.control

import static fnz.control.Where.check
import static fnz.data.Fn.val

import fnz.data.Maybe

import spock.lang.Unroll
import spock.lang.Specification

class WhereSpecificatione extends Specification {

    @Unroll
    void 'Build a where expression'() {
        when: 'An expression'
            // tag::simpleWhere[]
            Maybe<String> result = check(weight: value) {
                when { weight <= underweight } then { "You're underweight" }
                when { weight <= normal }      then { throw new Exception("You're normal") }
                when { weight <= fat }         then { "You're fat" }
                otherwise { "You have a strange composition" }
                where {
                    underweight = 50
                    normal      = 70
                    fat         = 90
                }
            }
            // end::simpleWhere[]
        then: 'Not exception has been thrown'
            notThrown(Exception)
        and: "The underweight output"
            result.isPresent() == true
            val(result) == expected
        where:
            value | expected
            50    | "You're underweight"
            90    | "You're fat"
    }

    void 'Throwing an exception'() {
        when: 'An expression'
            Maybe<String> result = check(weight: 70) {
                when { weight <= underweight } then { "You're underweight" }
                when { weight <= normal }      then { throw new Exception("You're normal") }
                when { weight <= fat }         then { "You're fat" }
                otherwise { "You have a strange composition" }
                where {
                    underweight = 50
                    normal      = 70
                    fat         = 90
                }
            }
        then: 'An exception has been thrown'
            thrown(Exception)
    }

    @Unroll
    void 'Shortening expressions [Only when-then]'() {
        when: 'There are only when-then expressions'
            // tag::whereOnlyWithWhenThen[]
            Maybe<String> result = check(weight: value) {
                when { weight <= 51 } then { "You're underweight" }
                when { weight <= 90 } then { "You're fat" }
            }
            // end::whereOnlyWithWhenThen[]
        then: 'I should be able to get the rigth result'
            val(result) == expected
        where: 'The test value is'
            value | expected
             51   | "You're underweight"
             90   | "You're fat"
    }

    @Unroll
    void 'Shortening expressions [when-then with otherwise]'() {
        when: 'There are only when-then expressions'
            // tag::whereWithNoWhere[]
            Maybe<String> result = check(weight: value) {
                when { weight <= 51 } then { "You're underweight" }
                when { weight <= 90 } then { "You're fat" }
                otherwise { "Default" }
            }
            // end::whereWithNoWhere[]
        then: 'I should be able to get the rigth result'
            val(result) == expected
        where: 'The test value is'
            value | expected
             51   | "You're underweight"
             90   | "You're fat"
             200  | "Default"
    }

    @Unroll
    void 'Build switch-case like statements (Classes)'() {
         given: 'An expression'
            // tag::whereSwitchLike1[]
            Maybe<String> result = check(value) {
                when String then { stringMessage }
                when Integer then { bigIntegerMessage }
                otherwise { otherwiseMessage }
                where {
                    stringMessage     = "Is a String"
                    bigIntegerMessage = "Is a Integer"
                    otherwiseMessage  = "No Idea"
                }
            }
            // end::whereSwitchLike1[]
        expect: "The underweight output"
            result.isPresent() == true
            val(result) == expected
        where:
            value | expected
            "hi"  | "Is a String"
            70    | "Is a Integer"
            null  | "No Idea"
    }

    @Unroll
    void 'Build switch-case like statements (Ranges)'() {
         given: 'An expression'
            // tag::whereSwitchLike2[]
            Maybe<String> result = check(value) {
                when 10..20 then { SMALL }
                when 20..30 then { MEDIUM }
                otherwise { BIG }
                where {
                    SMALL   = "Small"
                    MEDIUM  = "Medium"
                    BIG     = "No Idea"
                }
            }
            // end::whereSwitchLike2[]
        expect: "The underweight output"
            result.isPresent() == true
            val(result) == expected
        where:
            value | expected
            15    | "Small"
            25    | "Medium"
            null  | "No Idea"
    }

    @Unroll
    void 'Build switch-case like statements (Number)'() {
         given: 'An expression'
            // tag::whereSwitchLike3[]
            Maybe<String> result = check(value) {
                when 15 then { SMALL }
                when 25 then { MEDIUM }
                otherwise { BIG }
                where {
                    SMALL   = "Small"
                    MEDIUM  = "Medium"
                    BIG     = "No Idea"
                }
            }
            // end::whereSwitchLike3[]
        expect: "The underweight output"
            result.isPresent() == true
            val(result) == expected
        where:
            value | expected
            15    | "Small"
            25    | "Medium"
            null  | "No Idea"
    }
}
