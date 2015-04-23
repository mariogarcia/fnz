package fnz.ast.flow

import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX

import fnz.ast.MethodCallExpressionTransformer

import org.codehaus.groovy.control.SourceUnit

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression

class DoReturnAstTransformer extends MethodCallExpressionTransformer {

    static final String DO_RETURN_METHOD_NAME = '$return'
    static final String UNIT_METHOD_NAME = 'unit'

    private final String monadClassId

    /**
     * The $return method only does a call to the monad's unit(v) function.
     * In order to acomplish that it needs to know which is the name of the
     * monad variable in the available scope.
     *
     * @param  monadClassId the name of the monad variable
     * @param sourceUnit
     */
    DoReturnAstTransformer(String monadClassId, SourceUnit sourceUnit) {
        super(sourceUnit, DO_RETURN_METHOD_NAME)
        this.monadClassId = monadClassId
    }

    @Override
    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        return callX(
            varX(monadClassId),
            UNIT_METHOD_NAME,
            methodCallExpression.arguments
        )
    }

}
