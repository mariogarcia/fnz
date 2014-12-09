package fnz.control

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.ClassHelper.make

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.classgen.VariableScopeVisitor

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import fnz.data.Fn
import fnz.ast.MethodCallExpressionTransformer

class LetAstTransformer extends MethodCallExpressionTransformer {

    LetAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, 'let')
    }

    Expression transformMethodCall(MethodCallExpression expression) {
        MethodCallExpression methodCallExpression = (MethodCallExpression) expression
        ArgumentListExpression argumentListExpression = (ArgumentListExpression) methodCallExpression.arguments
        MapExpression mapExpression = (MapExpression) argumentListExpression.expressions.first()
        ClosureExpression fn = (ClosureExpression) argumentListExpression.expressions.last()

        return mapExpression.mapEntryExpressions.first().with { MapEntryExpression entry ->
            Expression value = entry.valueExpression
            ClassNode type = entry.valueExpression.type
            String key = entry.keyExpression.value

            // value could be a value or a closure
            // key should be the next closure argument

            return exp
        }
    }

    Expression getJustFrom(Expression value) {
        return new StaticMethodCallExpression(
            make(Fn, false),
            'Just',
            args(value)
        )
    }

}
