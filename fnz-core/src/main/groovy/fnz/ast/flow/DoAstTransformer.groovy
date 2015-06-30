package fnz.ast.flow

import static fnz.ast.AstUtils.getArgs
import static fnz.ast.AstUtils.isToken
import static fnz.ast.AstUtils.uniqueIdentifier
import static fnz.ast.AstUtils.isClosureExpression
import static fnz.ast.AstUtils.isBinaryExpression

import static org.codehaus.groovy.ast.ClassHelper.make

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX

import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric

import fnz.ast.AstUtils
import fnz.ast.MethodCallExpressionTransformer

import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.VariableExpression

import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.control.SourceUnit

class DoAstTransformer extends MethodCallExpressionTransformer {

    static final String DO_METHOD_NAME = '$do'
    static final String BIND_METHOD_NAME = 'bind2'
    static final String DO_CALL_METHOD_NAME = 'doCall'
    static final String WILDCARD_NAME = '_'

    DoAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, DO_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        ArgumentListExpression args = getArgs(methodCallExpression)

        if (!checkParams(args)) return

        ClosureExpression closureExpression = args.first()
        Expression bindExpression = processClosure(closureExpression)

        return bindExpression
    }

    Boolean checkParams(final ArgumentListExpression args) {
        if (!isClosureExpression(args.first())) {
            addError(args.first(), "\$do only accepts a Closure as argument")
            return false
        }

        return true
    }

    Expression processClosure(final ClosureExpression closureExpression) {
        List<ExpressionStatement> expressionStatementList = extractExpressionStatementList(closureExpression).reverse()
        Statement returnStatement                         = extractReturnStatement(closureExpression)
        Expression returnExpression                       = returnStatement.expression
        List<Expression> bindExpressionList               = extractBindAssignmentExpressionList(expressionStatementList)

        return combine(bindExpressionList, returnExpression)
    }

    List<BinaryExpression> extractBindAssignmentExpressionList(List<ExpressionStatement> expressionStatementList) {
        return expressionStatementList*.expression.findAll(this.&isAssignmentExpression)
    }

    Boolean isAssignmentExpression(Expression expression) {
        return isBinaryExpression(expression) && isToken(expression.operation, Types.ASSIGN)
    }

    List<ExpressionStatement> extractExpressionStatementList(ClosureExpression closureExpression) {
        return closureExpression.code.statements.findAll(AstUtils.&isExpressionStatement)
    }

    // TODO check last node is $return call
    Statement extractReturnStatement(final ClosureExpression closureExpression) {
        return closureExpression.code.statements.last()
    }

    Expression combine(final List<BinaryExpression> bindingList, final Expression returnExpression) {
        return bindingList.inject(returnExpression, this.&evaluateBinaryExpression)
    }

    /**
     * Within the execution of this method the $return expression will be evaluated
     * if present
     *
     * @param previous if first expression evaluated it will be the last expression (return or $return)
     * @param next the next binding expression to evaluate
     * @return a bind2(v, fn) expression
     *
     */
    Expression evaluateBinaryExpression(final Expression previous, final BinaryExpression next) {
        VariableExpression nextKey          = next.leftExpression
        String closureVarName               = changeIfWildCard(nextKey.name)
        String closureClazzId               = uniqueIdentifier
        Expression nextValue                = next.rightExpression
     /* We need to make sure $return has been transformed */
        ClassCodeExpressionTransformer trs  = new DoReturnAstTransformer(closureClazzId, sourceUnit)
        Expression previousVisited          = trs.transform(previous)
        Statement stmt                      = getStatementFrom(previousVisited)
        ClosureExpression closureExpression = getClosureExpression(closureClazzId, closureVarName, stmt)

        return getBindExpression(nextValue, closureExpression)
    }

    /**
     * Although in Haskell assignment expressions could be mixed with other type of expressions
     * here the do notation only accepts assignment expressions and $return expressions so in order
     * to write expressions not returning anything we could write expressions assigned to the '_'
     * symbol, meaning we don't care about the returning value:
     *
     * <pre>
     * $do {
     *    x = Just(1)
     *    _ = printJust("x has value $x")
     *    y = Just(x + 1)
     *
     *    $return y
     * }
     *
     * @param possibleWildcard the variable name used for the assignment
     * @return an unique identifier in case the wildcard was used as variable name
     */
    String changeIfWildCard(String possibleWildcard) {
        return possibleWildcard == WILDCARD_NAME ? uniqueIdentifier : possibleWildcard
    }

    /**
     * This method creates a closure with two arguments a class identifying
     * the type of monad that can be returned by the bind expression and
     * the monadic value carried by the expression
     *
     * @param clazzId The id of the class instance
     * @param singleParamName the name of the param carrying the unwrapped monadic value
     * @param block the body of the statement
     * @return the full closure expression
     */
    ClosureExpression getClosureExpression(
        final String clazzId,
        final String singleParamName,
        final Statement block) {
        ClosureExpression closureExpression =
            closureX(
                params(
                    param(nonGeneric(make(Class)), clazzId),
                    param(make(Object), singleParamName)
                ),
                block
            )

        closureExpression.variableScope = new VariableScope()

        return closureExpression
    }

    Statement getStatementFrom(final Expression expression) {
        return block(stmt(expression))
    }

    private MethodCallExpression getBindExpression(
        final Expression value, final ClosureExpression closureWithKey) {

        return isClosureExpression(value) ?
            callX(callX(value, DO_CALL_METHOD_NAME), BIND_METHOD_NAME, args(closureWithKey)) :
            callX(value, BIND_METHOD_NAME, args(closureWithKey))
    }

}
