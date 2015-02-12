package fnz.ast

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression

import org.codehaus.groovy.control.SourceUnit
import fnz.ast.DefaultClassCodeExpressionTransformer

/**
 * This class can be used as a base to transform certain MethodCallExpression instances.
 *
 * Let's say we had a expression like:
 * <pre>inventedmethod(x:1) {}</pre>
 * We can transform that expression into another creating an instance of a MethodCallExpression:
 * <pre>
 * class InventedMethodTransformer extends MethodCallExpressionTransformer {
 *       InventedMethodTransformer(SourceUnit sourceUnit) {
 *           super(sourceUnit, 'inventedMethod')
 *       }
 *
 *       Expression transformMethodCall(MethodCallExpression methodCallExpression) {
 *           // implement the transformation
 *       }
 * }
 * </pre>
 * @author Mario Garcia
 */
abstract class MethodCallExpressionTransformer extends DefaultClassCodeExpressionTransformer {

    String methodCallName

    /**
     * Every instance needs the source unit awareness and the name of the method
     * it's going to transform
     *
     * @param sourceUnit Needed to apply scope
     * @param name The name of the method you want to transform
     */
    MethodCallExpressionTransformer(SourceUnit sourceUnit, String name) {
         super(sourceUnit)
         this.methodCallName = name
    }

    @Override
    Expression transform(Expression expression) {
        if (!expression) return

        Boolean isTheMethod =
            expression instanceof MethodCallExpression &&
            expression.methodAsString == this.methodCallName

        return isTheMethod ?
        this.transformMethodCall(expression) :
        expression.transformExpression(this)
    }

    /**
     * This method will transform the expression into its final version.
     *
     * @param methodCallExpression the method expression you want to transform
     * @return the final version of the method expression
     */
    abstract Expression transformMethodCall(MethodCallExpression methodCallExpression)

}
