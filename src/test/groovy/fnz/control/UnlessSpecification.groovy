package fnz.control

import static fnz.data.Fn.val
import static fnz.control.Unless.ret
import static fnz.control.Unless.unless

import fnz.data.Maybe
import spock.lang.Unroll
import spock.lang.Specification

class UnlessSpecification extends Specification {

    @Unroll
    def 'Execute unless-block'() {
        when: 'Trying to do something unless value is less equals than 0'
            // tag::simpleUnless[]
            Maybe<Integer> result =  unless (xparam <= 0) {
                return 3 + xparam
            }
            // end::simpleUnless[]
        then: 'We should be getting the expected value'
            val(result) == expected
        where: 'Possible values are'
            xparam | expected
            1      | 4
            2      | 5
            0      | null
    }

}
