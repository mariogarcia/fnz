package fnz.ast.flow

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
            unless(xparam == 2) {
               def nested =
                   unless(xparam == 1) {
                      3 + xparam
                   }
               val(nested)
            }

        return val(result)
    }

}
