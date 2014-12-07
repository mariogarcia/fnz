package fnz.control

import static fnz.data.Fn.val
import fnz.data.Maybe

class UnlessSpecExample {

    Integer basicUnlessExample(Integer xparam) {
        // tag::simpleUnless[]
        Maybe<Integer> result =
            unless(xparam <= 0) {
                return 3 + xparam
            }
        // end::simpleUnless[]
        return val(result)
    }

    Integer nestedUnlessExample(Integer xparam) {
        Maybe<Integer> result =
            unless(xparam <= 2) {
               unless(xparam >= 1) {
                   return 3 + xparam
               }
            }

        return val(result)
    }

}
