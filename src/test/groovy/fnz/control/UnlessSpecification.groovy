package fnz.control

import static fnz.control.Unless.ret
import static fnz.control.Unless.unless

import fnz.base.Option
import spock.lang.Unroll
import spock.lang.Specification

class UnlessSpecification extends Specification {

    @Unroll
    def 'Execute unless-block'() {
        when: 'Trying to do something unless value is less equals than 0'
            // tag::simpleUnless[]
            Option<Integer> result =  unless (xparam <= 0) {
                return 3 + xparam
            }
            // end::simpleUnless[]
        then: 'We should be getting the expected value'
            result.get() == expected
        where: 'Possible values are'
            xparam | expected
            1      | 4
            2      | 5
            0      | null
    }

}
