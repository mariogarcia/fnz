package fnz.ast

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX

import groovy.transform.CompileStatic

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import org.codehaus.groovy.classgen.VariableScopeVisitor

@CompileStatic
final class AstUtils {

    static final String PLACEHOLDER_NAME = '_'
    static final String DO_CALL_METHOD_NAME = 'doCall'

    static void applyScopeVisitor(final MethodCallExpression expression, final SourceUnit sourceUnit) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitMethodCallExpression(expression)
    }

    static void applyScopeVisitor(final StaticMethodCallExpression expression, final SourceUnit sourceUnit) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitStaticMethodCallExpression(expression)
    }

    static Expression callClosureX(final ClosureExpression closure) {
        return callX(closure, DO_CALL_METHOD_NAME)
    }

    static Expression callClosureX(final ClosureExpression closure, ArgumentListExpression args) {
        return callX(closure, DO_CALL_METHOD_NAME, args)
    }

    static Boolean isClosureExpression(final Expression expression) {
        return expression instanceof ClosureExpression
    }

    static Boolean isBinaryExpression(final Expression expression) {
        return expression instanceof BinaryExpression
    }

    static Boolean isConstantExpression(final Expression expression) {
        return expression instanceof ConstantExpression
    }

    static ArgumentListExpression getArgs(final MethodCallExpression methodCallExpression) {
        return (ArgumentListExpression) methodCallExpression.arguments
    }

    static <U extends Expression> U getFirstArgumentAs(final ArgumentListExpression args, Class<U> asType) {
        return asType.cast(args.expressions.first())
    }

    static <U extends Expression> U getLastArgumentAs(final ArgumentListExpression args, Class<U> asType) {
        return asType.cast(args.expressions.last())
    }

    static Boolean isPlaceHolder(String variableName) {
        return variableName == PLACEHOLDER_NAME
    }

    static String getUniqueIdentifier() {
        return "_${System.nanoTime()}"
    }

}
