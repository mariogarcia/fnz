package fnz.ast.flow

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

import static fnz.ast.AstUtils.applyScopeVisitor
import static fnz.ast.AstUtils.callClosureX
import static fnz.ast.AstUtils.isClosureExpression
import static fnz.ast.AstUtils.getArgs
import static fnz.ast.AstUtils.getFirstArgumentAs
import static fnz.ast.AstUtils.getLastArgumentAs

import fnz.ast.MethodCallExpressionTransformer
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import org.codehaus.groovy.ast.VariableScope

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.SourceUnit

/**
 *
 * This transformer will transform expressions like this one:
 * <pre>
 * let(x:1, y: { x + 1}) {
 *     y + 1
 * }
 * </pre>
 * into this one:
 * <pre>
 * { x ->
 *   { y ->
 *     { y + 1}()
 *   }({x + 1}())
 * }(1)
 * </pre>
 *
 * @author Mario Garcia
 *
 */
@CompileStatic
class LetAstTransformer extends MethodCallExpressionTransformer {

    static final String LET_METHOD_NAME = 'let'

    LetAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, LET_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression args                   = getArgs(methodCallExpression)
        MapExpression mapExpression                   = getFirstArgumentAs(args, MapExpression)
        List<MapEntryExpression> mapEntryExpressions  = mapExpression.mapEntryExpressions.reverse()
        ClosureExpression fn                          = getLastArgumentAs(args, ClosureExpression)

        // checking if there is another nested let expression
        this.visitClosureExpression(fn)
        // processing this let expression
        MethodCallExpression result = (MethodCallExpression) mapEntryExpressions.inject(fn, this.&evaluateMapEntryExpression)
        // fixing variable scope
        applyScopeVisitor(result, sourceUnit)

        return result
    }

    @CompileDynamic
    private Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey          = (ConstantExpression) next.keyExpression
        String closureVarName               = nextKey.value.toString()
        Expression nextValue                = next.valueExpression
        Statement previousStatement         = createStatementFrom(previous)
        Expression processedValue           = processValueExpression(nextValue)
        ClosureExpression closureExpression = createClosure(closureVarName, previousStatement)

        return callClosureX(closureExpression, args(processedValue))
    }

    ClosureExpression createClosure(final String singleParamName, final Statement block) {
        ClosureExpression closureExpression = closureX(params(param(make(Object), singleParamName)), block)
        closureExpression.variableScope = new VariableScope()

        return closureExpression
    }

    Statement createStatementFrom(final Expression expression) {
        return stmt(expression)
    }

    Statement createStatementFrom(final ClosureExpression expression) {
        return stmt(callClosureX(expression))
    }

    Expression processValueExpression(final Expression expression) {
        return expression
    }

    Expression processValueExpression(final ClosureExpression expression) {
        return callClosureX(expression)
    }

}
