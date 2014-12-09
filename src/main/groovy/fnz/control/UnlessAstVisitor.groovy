package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.args

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import org.codehaus.groovy.control.SourceUnit
import fnz.ast.MethodCallExpressionTransformer

class UnlessAstVisitor extends MethodCallExpressionTransformer {

    UnlessAstVisitor(SourceUnit sourceUnit) {
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

        StaticMethodCallExpression finalExpression =
            new StaticMethodCallExpression(
                make(Unless, false),
                     methodCallName,
                     args(booleanExpression,bodyExpression))

        return finalExpression

    }

}
