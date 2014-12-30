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
    static final String DO_CALL_METHOD_NAME = 'doCall'

    LetAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, LET_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression argumentListExpression = (ArgumentListExpression) methodCallExpression.arguments

        MapExpression mapExpression = (MapExpression) argumentListExpression.expressions.first()
        List<MapEntryExpression> mapEntryExpressions = mapExpression.mapEntryExpressions.reverse()
        ClosureExpression fn = (ClosureExpression) argumentListExpression.expressions.last()

        this.visitClosureExpression(fn)
        MethodCallExpression result = (MethodCallExpression) mapEntryExpressions.inject(fn, this.&evaluateMapEntryExpression)
        this.applyScopeVisitor(result)

        return result
    }

    void applyScopeVisitor(final MethodCallExpression expression) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitMethodCallExpression(expression)
    }


    Expression evaluateMapEntryExpression(final Expression previous, final MapEntryExpression next) {
        ConstantExpression nextKey = (ConstantExpression) next.keyExpression
        String closureVarName = nextKey.value.toString()
        Expression nextValue = next.valueExpression
        Statement previousStatement = previous instanceof ClosureExpression ? stmt(invokeClosure(previous)) : stmt(previous)
        Expression processedValue = nextValue instanceof ClosureExpression ? invokeClosure(nextValue) : nextValue

        ClosureExpression closureExpression = closureX(params(param(make(Object), closureVarName)), previousStatement)
        closureExpression.variableScope = new VariableScope()

        return callX(closureExpression, DO_CALL_METHOD_NAME, args(processedValue))
    }

    Expression invokeClosure(ClosureExpression closure) {
        return callX(closure, DO_CALL_METHOD_NAME)
    }

}
