package fnz.ast

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX

import groovy.transform.CompileStatic

import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.control.SourceUnit

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

import org.codehaus.groovy.classgen.VariableScopeVisitor

@CompileStatic
final class AstUtils {

    static final String DO_CALL_METHOD_NAME = 'doCall'

    static void applyScopeVisitor(final MethodCallExpression expression, final SourceUnit sourceUnit) {
        VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit)
        variableScopeVisitor.prepareVisit(sourceUnit.AST.scriptClassDummy)
        variableScopeVisitor.visitMethodCallExpression(expression)
    }

    static Expression callClosureX(final ClosureExpression closure) {
        return callX(closure, DO_CALL_METHOD_NAME)
    }

    static Expression callClosureX(final ClosureExpression closure, ArgumentListExpression args) {
        return callX(closure, DO_CALL_METHOD_NAME, args)
    }

    static Boolean isBinaryExpression(final Expression expression) {
        return expression instanceof BinaryExpression
    }

    static Boolean isClosureExpression(final Expression expression) {
        return expression instanceof ClosureExpression
    }

    static Boolean isExpressionStatement(final Statement statement) {
        return statement instanceof ExpressionStatement
    }

    static ArgumentListExpression getArgs(final MethodCallExpression methodCallExpression) {
        return (ArgumentListExpression) methodCallExpression.arguments
    }

    static <U extends Expression> U getFirstArgumentAs(final ArgumentListExpression args, Class<U> asType) {
        return asType.cast(args.expressions.first())
    }

    static Boolean isToken(Token token, int typeRef) {
        return token.type == typeRef
    }

    static <U extends Expression> U getLastArgumentAs(final ArgumentListExpression args, Class<U> asType) {
        return asType.cast(args.expressions.last())
    }

    static String getUniqueIdentifier() {
        return "_${System.nanoTime()}"
    }

}
