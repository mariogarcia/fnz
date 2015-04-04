package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.SourceUnit

import fnz.data.Fn
import fnz.ast.MethodCallExpressionTransformer

/**
 * This transformer transforms expressions like this:
 * <pre>
 * unless(something == 2) {
 *    executeSomethingElse()
 * }
 * </pre>
 * into this:
 * <pre>
 * if (!(something == 2)) {
 *    executeSomethingElse()
 * }
 * </pre>
 *
 * @author Mario Garcia
 *
 */
@CompileStatic
class UnlessAstTransformer extends MethodCallExpressionTransformer {

    static final String JUST = 'Just'
    static final String NOTHING = 'Nothing'
    static final String DO_CALL = 'doCall'

    UnlessAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, 'unless')
    }

    @Override
    Expression transformMethodCall(MethodCallExpression unlessExpression) {
        ArgumentListExpression argsExpression = (ArgumentListExpression) unlessExpression.arguments
        Expression booleanExpression = argsExpression.expressions.first()
        ClosureExpression bodyExpression = (ClosureExpression) argsExpression.expressions.last()

        // This introspects closure structure and applies
        // transform(...) on all nodes
        this.visitClosureExpression(bodyExpression)

        return ternaryX(
            notX(booleanExpression),
            callX(make(Fn, false), JUST, callX(bodyExpression, DO_CALL)),
            callX(make(Fn, false), NOTHING)
        )

    }

}
