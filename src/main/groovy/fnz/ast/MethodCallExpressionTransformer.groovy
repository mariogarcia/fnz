package fnz.ast

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression

import org.codehaus.groovy.control.SourceUnit
import fnz.ast.DefaultClassCodeExpressionTransformer

abstract class MethodCallExpressionTransformer extends DefaultClassCodeExpressionTransformer {

    String methodCallName

    MethodCallExpressionTransformer(SourceUnit sourceUnit, String name) {
         super(sourceUnit)
         this.methodCallName = name
    }

    @Override
    Expression transform(Expression expression) {
         if (expression instanceof MethodCallExpression &&
             expression.methodAsString == this.methodCallName) {
             return this.transformMethodCall(expression)
         }

         return expression.transformExpression(this)
    }

    abstract Expression transformMethodCall(MethodCallExpression methodCallExpression)

}
