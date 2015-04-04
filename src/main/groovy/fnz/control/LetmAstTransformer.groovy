package fnz.control

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX

import fnz.ast.MethodCallExpressionTransformer
import fnz.data.Fn
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.VariableScope

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
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
class LetmAstTransformer extends MethodCallExpressionTransformer {

    static final String LET_METHOD_NAME = 'letm'
    static final String BIND_METHOD_NAME = 'bind'
    static final String DO_CALL_METHOD_NAME = 'doCall'

    LetmAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, LET_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression argumentListExpression = (ArgumentListExpression) methodCallExpression.arguments

        MapExpression mapExpression = (MapExpression) argumentListExpression.expressions.first()
        List<MapEntryExpression> mapEntryExpressions = mapExpression.mapEntryExpressions.reverse()
        ClosureExpression fn = (ClosureExpression) argumentListExpression.expressions.last()

        StaticMethodCallExpression finalExpression = loopThroughEntryExpressions(mapEntryExpressions, fn)

        this.visitClosureExpression(fn)
        this.applyScopeVisitor(finalExpression)

        return finalExpression

    }

    private StaticMethodCallExpression loopThroughEntryExpressions(
        final List<MapEntryExpression> expressions, final ClosureExpression fn) {
        return (StaticMethodCallExpression) expressions.inject(fn, this.&evaluateMapEntryExpression)
    }

    private void applyScopeVisitor(final StaticMethodCallExpression expression) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitStaticMethodCallExpression(expression)
    }

    private Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey = (ConstantExpression) next.keyExpression
        String closureVarName = nextKey.value.toString()
        Expression nextValue = next.valueExpression
        Statement stmt = previous instanceof ClosureExpression ? previous.code : block(stmt(previous))

        ClosureExpression closureExpression = closureX(params(param(make(Object), closureVarName)), stmt)
        closureExpression.variableScope = new VariableScope()

        return getBindExpression(nextValue, closureExpression)
    }

    private StaticMethodCallExpression getBindExpression(
        final Expression value, final ClosureExpression closureWithKey) {
        return value instanceof ClosureExpression ?
        callX(make(Fn), BIND_METHOD_NAME, args(callX(value, DO_CALL_METHOD_NAME), closureWithKey)) :
        callX(make(Fn), BIND_METHOD_NAME, args(value, closureWithKey))
    }

}
