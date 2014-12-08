package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import org.codehaus.groovy.control.SourceUnit

import groovy.transform.InheritConstructors
import fnz.ast.DefaultClassCodeExpressionTransformer

@InheritConstructors
class UnlessAstVisitor extends DefaultClassCodeExpressionTransformer {

    static final String UNLESS = 'unless'

    @Override
    Expression transform(Expression expression) {
         if (expression instanceof MethodCallExpression && expression.methodAsString == UNLESS) {

             MethodCallExpression unlessExpression = (MethodCallExpression) expression
             ArgumentListExpression argsExpression = (ArgumentListExpression) unlessExpression.arguments
             Expression booleanExpression = argsExpression.expressions.first()
             ClosureExpression bodyExpression = (ClosureExpression) argsExpression.expressions.last()

             // This introspects closure structure and applies
             // transform(...) on all nodes
             this.visitClosureExpression(bodyExpression)

             StaticMethodCallExpression finalExpression =
                 new StaticMethodCallExpression(
                     make(Unless, false),
                     UNLESS,
                     new ArgumentListExpression(
                         booleanExpression,
                         bodyExpression))

             return finalExpression
         }

         return expression.transformExpression(this)
    }

}
