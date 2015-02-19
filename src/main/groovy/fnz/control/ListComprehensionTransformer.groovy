package fnz.control

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression

import fnz.ast.ListExpressionTransformer

class ListComprehensionTransformer extends ListExpressionTransformer {

    @Override
    Boolean isThisListEligible(ListExpression listExpression) {
        return false
    }

    @Override
    Expression transformListExpression(ListExpression listExpression) {
        return null
    }

}
