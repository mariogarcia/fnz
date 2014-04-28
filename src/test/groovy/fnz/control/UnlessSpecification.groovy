package fnz.control

import static fnz.control.Unless.check
import static fnz.control.Unless.unless

import spock.lang.Unroll
import spock.lang.Specification

class UnlessSpecification extends Specification {

    @Unroll
    def 'Execute check-block-unless'() {
        when: 'Trying to check a statement unless certain value is less equals than 0'
            def result = check { return 3 + xparam } unless (xparam <= 0)
        then: 'We should be getting the expected value'
            result == expected
        where: 'Possible values are'
            xparam | expected
            1      | 4
            2      | 5
            0      | null
    }

    @Unroll
    def 'Execute unless-block'() {
        when: 'Trying to do something unless value is less equals than 0'
            def result =  unless (xparam <= 0) {
                return 3 + xparam
            }
        then: 'We should be getting the expected value'
            result == expected
        where: 'Possible values are'
            xparam | expected
            1      | 4
            2      | 5
            0      | null
    }

}