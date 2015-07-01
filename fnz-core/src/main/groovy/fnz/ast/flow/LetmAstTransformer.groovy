package fnz.ast.flow

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX

import static fnz.ast.AstUtils.applyScopeVisitor
import static fnz.ast.AstUtils.getArgs
import static fnz.ast.AstUtils.getFirstArgumentAs
import static fnz.ast.AstUtils.getLastArgumentAs

import fnz.ast.MethodCallExpressionTransformer
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import org.codehaus.groovy.ast.VariableScope

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit

/**
 *
 * This transformer will transform expressions like this one:
 * <pre>
 * letm(x:Just(1), y: { Just(x + 1) } ) {
 *     Just(y + 1)
 * }
 * </pre>
 * into this one:
 * <pre>
 * bind(Just(1)) { x ->
 *    bind({ Just(x + 1) }()) { y ->
 *        Just(y + 1)
 *    }
 * }
 *</pre>
 *
 * The deepest expression should always return a fnz.data.Monad instance
 * (of course or any of its children).
 *
 * @author Mario Garcia
 *
 */
@CompileStatic
@SuppressWarnings('FactoryMethodName')
class LetmAstTransformer extends MethodCallExpressionTransformer {

    static final String LET_METHOD_NAME = 'letm'
    static final String BIND_METHOD_NAME = 'bind'
    static final String DO_CALL_METHOD_NAME = 'doCall'

    LetmAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, LET_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression args                  = getArgs(methodCallExpression)
        MapExpression mapExpression                  = getFirstArgumentAs(args, MapExpression)
        List<MapEntryExpression> mapEntryExpressions = mapExpression.mapEntryExpressions.reverse()
        ClosureExpression fn                         = getLastArgumentAs(args, ClosureExpression)

        MethodCallExpression finalExpression = loopThroughEntryExpressions(mapEntryExpressions, fn)

        this.visitClosureExpression(fn)
        applyScopeVisitor(finalExpression, sourceUnit)

        return finalExpression
    }

    private MethodCallExpression loopThroughEntryExpressions(
        final List<MapEntryExpression> expressions,
        final ClosureExpression fn) {
        return (MethodCallExpression) expressions.inject(fn, this.&evaluateMapEntryExpression)
    }

    @CompileDynamic
    private Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey          = (ConstantExpression) next.keyExpression
        String closureVarName               = nextKey.value.toString()
        Expression nextValue                = next.valueExpression
        Statement stmt                      = createStatementFrom(previous)
        ClosureExpression closureExpression = createClosure(closureVarName, stmt)

        return getBindExpression(nextValue, closureExpression)
    }

    ClosureExpression createClosure(final String singleParamName, final Statement block) {
        ClosureExpression closureExpression = closureX(params(param(make(Object), singleParamName)), block)
        closureExpression.variableScope = new VariableScope()

        return closureExpression
    }

    Statement createStatementFrom(final Expression expression) {
        return block(stmt(expression))
    }

    Statement createStatementFrom(final ClosureExpression expression) {
        return expression.code
    }

    private MethodCallExpression getBindExpression(
        final Expression value, final ClosureExpression closureWithKey) {
        return value instanceof ClosureExpression ?
            callX(callX(value, DO_CALL_METHOD_NAME), BIND_METHOD_NAME, args(closureWithKey)) :
            callX(value, BIND_METHOD_NAME, args(closureWithKey))
    }

}
