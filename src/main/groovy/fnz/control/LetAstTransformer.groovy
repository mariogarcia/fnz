package fnz.control

import fnz.ast.MethodCallExpressionTransformer
import fnz.data.Fn
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.SourceUnit

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
class LetAstTransformer extends MethodCallExpressionTransformer {

    static final String LET_METHOD_NAME = 'let'
    static final String BIND_METHOD_NAME = 'bind'
    static final String JUST_METHOD_NAME = 'Just'
    static final String DO_CALL_METHOD_NAME = 'doCall'

    LetAstTransformer(SourceUnit sourceUnit) {
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

    StaticMethodCallExpression loopThroughEntryExpressions(final List<MapEntryExpression> expressions, final ClosureExpression fn) {
        return (StaticMethodCallExpression) expressions.inject(fn, this.&evaluateMapEntryExpression)
    }

    void applyScopeVisitor(final StaticMethodCallExpression expression) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitStaticMethodCallExpression(expression)
    }

    Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey = (ConstantExpression) next.keyExpression
        String closureVarName = nextKey.value.toString()
        Expression nextValue = next.valueExpression
        Statement stmt = previous instanceof ClosureExpression ? previous.code : block(stmt(previous))

        ClosureExpression closureExpression = closureX(params(param(make(Object), closureVarName)), stmt)
        closureExpression.variableScope = new VariableScope()

        return buildBindExpression(nextValue, closureExpression)
    }

    StaticMethodCallExpression buildBindExpression(final Expression value, final ClosureExpression closureWithKey) {
        return value instanceof ClosureExpression ?
        callX(make(Fn), BIND_METHOD_NAME, args(getJustFrom(callX(value, DO_CALL_METHOD_NAME)), closureWithKey)) :
        callX(make(Fn), BIND_METHOD_NAME, args(getJustFrom(value), closureWithKey))
    }

    StaticMethodCallExpression getJustFrom(final Expression value) {
        return callX(make(Fn), JUST_METHOD_NAME, args(value))
    }

}
