package fnz.ast

import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import spock.lang.Unroll
import spock.lang.Specification

/**
 */
class AstUtilsSpec extends Specification {

    @Unroll
    void 'check proper token'() {
        when: 'asking about if a given token has a given id'
            Boolean result =
                AstUtils.isToken(
                    new Token(Types.COMPARE_GREATER_THAN_EQUAL, '>=', 0, 0),
                    type
                )
        then: 'the method should respond properly'
            result == correct
        where: 'possible values are'
            type                             |          correct
            Types.COMPARE_GREATER_THAN_EQUAL |           true
            Types.COMPARE_GREATER_THAN       |           false
    }
}
