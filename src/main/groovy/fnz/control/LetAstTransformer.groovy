package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

import fnz.ast.MethodCallExpressionTransformer
import groovy.transform.CompileStatic
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
    static final String DO_CALL_METHOD_NAME = 'doCall'

    LetAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, LET_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression argumentListExpression = (ArgumentListExpression) methodCallExpression.arguments
        MapExpression mapExpression = (MapExpression) argumentListExpression.expressions.first()
        // We need to evaluate expression in reverse order
        List<MapEntryExpression> mapEntryExpressions = mapExpression.mapEntryExpressions.reverse()
        ClosureExpression fn = (ClosureExpression) argumentListExpression.expressions.last()

        // checking if there is another nested let expression
        this.visitClosureExpression(fn)
        // processing this let expression
        MethodCallExpression result =
            (MethodCallExpression) mapEntryExpressions.inject(fn, this.&evaluateMapEntryExpression)
        // fixing variable scope
        this.applyScopeVisitor(result)

        return result
    }

    private void applyScopeVisitor(final MethodCallExpression expression) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitMethodCallExpression(expression)
    }

    private Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey = (ConstantExpression) next.keyExpression
        String closureVarName = nextKey.value.toString()
        Expression nextValue = next.valueExpression

        Statement previousStatement =
            previous instanceof ClosureExpression ? stmt(invokeClosure(previous)) : stmt(previous)
        Expression processedValue =
            nextValue instanceof ClosureExpression ? invokeClosure(nextValue) : nextValue

        // we're building a closure having a parameter with the same name as the map entry key
        // and the body is the value or the closure of the map entry value
        ClosureExpression closureExpression = closureX(params(param(make(Object), closureVarName)), previousStatement)
        closureExpression.variableScope = new VariableScope()

        // finally the closure is executed within the right scope
        return callX(closureExpression, DO_CALL_METHOD_NAME, args(processedValue))
    }

    private Expression invokeClosure(final ClosureExpression closure) {
        return callX(closure, DO_CALL_METHOD_NAME)
    }

}
