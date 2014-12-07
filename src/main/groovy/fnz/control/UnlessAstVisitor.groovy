package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.classgen.VariableScopeVisitor

import groovy.transform.InheritConstructors
import fnz.ast.DefaultClassCodeExpressionTransformer

@InheritConstructors
class UnlessAstVisitor extends DefaultClassCodeExpressionTransformer {

    Expression transform(Expression expression) {
         if (expression instanceof MethodCallExpression && expression.methodAsString == 'unless') {

             MethodCallExpression unlessExpression = (MethodCallExpression) expression
             ArgumentListExpression argsExpression = (ArgumentListExpression) unlessExpression.arguments
             Expression booleanExpression = argsExpression.expressions.first()
             ClosureExpression bodyExpression = (ClosureExpression) argsExpression.expressions.last()

             StaticMethodCallExpression finalExpression =
                 new StaticMethodCallExpression(
                     make(Unless, false),
                     'unless',
                     new ArgumentListExpression(
                         booleanExpression,
                         bodyExpression))

             return finalExpression
         }

         return expression.transformExpression(this)
    }

}
