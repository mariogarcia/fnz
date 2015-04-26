package fnz.ast.flow

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX

import static fnz.ast.AstUtils.getArgs
import static fnz.ast.AstUtils.callClosureX
import static fnz.ast.AstUtils.getLastArgumentAs

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.SourceUnit

import fnz.Fnz
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

    UnlessAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, 'unless')
    }

    @Override
    Expression transformMethodCall(MethodCallExpression unlessExpression) {
        ArgumentListExpression args  = getArgs(unlessExpression)
        Expression booleanExpression = args.first()
        ClosureExpression bodyExpression = getLastArgumentAs(args, ClosureExpression)

        // This introspects closure structure and applies
        // transform(...) on all nodes
        this.visitClosureExpression(bodyExpression)

        return ternaryX(
            notX(booleanExpression),
            callX(make(Fnz, false), JUST, callClosureX(bodyExpression)),
            callX(make(Fnz, false), NOTHING)
        )

    }

}
