package fnz.control

import fnz.ast.MethodCallExpressionTransformer
import fnz.data.Fn
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.SourceUnit

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
class TypeAstTransformer extends MethodCallExpressionTransformer {

    static final String TYPE_METHOD_NAME = 'type'

    TypeAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, TYPE_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        Expression binaryExpression = firstArgumentExpressionFrom(methodCallExpression)

        if (isNotABinaryExpression(binaryExpression)) {
            error(sourceUnit, binaryExpression)
            return methodCallExpression
        }


        return constX(42)
    }

    Expression firstArgumentExpressionFrom(MethodCallExpression methodCallExpression) {
        ArgumentListExpression args = (ArgumentListExpression) methodCallExpression.arguments

        return args.first()
    }

    boolean isNotABinaryExpression(Expression expression) {
        return !(expression instanceof BinaryExpression)
    }

    void error(SourceUnit sourceUnit, ASTNode node) {
        sourceUnit
        .addError(
            new SyntaxException(
                "Expected binary expression here. Something like: Fn(A) >> String >> A",
                node.columnNumber,
                node.lineNumber
            )
        )
    }

}
