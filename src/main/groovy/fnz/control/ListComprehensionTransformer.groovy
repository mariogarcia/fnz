package fnz.control

import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.MethodCallExpression

import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static org.codehaus.groovy.ast.ClassHelper.make

import fnz.ast.ListExpressionTransformer
import groovy.transform.InheritConstructors
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.syntax.Types

@InheritConstructors
class ListComprehensionTransformer extends ListExpressionTransformer {

    static final String METHOD_NAME_COLLECT_MANY = 'collectMany'
    static final String METHOD_NAME_DO_CALL = 'doCall'

    @Override
    Boolean isThisListEligible(ListExpression listExpression) {
        List<Expression> expressions = listExpression.expressions

        if(isAnEmptyListExpression(expressions)){
            return false
        }

        if (!isAPipeBinaryExpression(expressions.first())) {
            return false
        }

        return true
    }

    Boolean isAnEmptyListExpression(List<Expression> listExpression) {
        return  listExpression.isEmpty()
    }

    Boolean isAPipeBinaryExpression(Expression expression) {
        return isABinaryExpression(expression) &&
            asBinary(expression).operation.type == Types.BITWISE_OR
    }

    Boolean isAGeneratorExpression(Expression expression) {
        return isABinaryExpression(expression) &&
            asBinary(expression).operation.type == Types.LEFT_SHIFT
    }

    Boolean isABinaryExpression(Expression expression) {
        return expression instanceof BinaryExpression
    }

    BinaryExpression asBinary(Expression expression) {
        return (BinaryExpression) expression
    }

    @Override
    Expression transformListExpression(ListExpression listExpression) {
        List<Expression> expressions = listExpression.expressions

        // ------------------- FIRST EXPRESSION --------------------
        BinaryExpression firstExpression = asBinary(expressions.head())
        Expression variables = firstExpression.leftExpression
        BinaryExpression generator = asBinary(firstExpression.rightExpression)

        // TODO [].plus() is not recognized by @CompileStatic...yet
        List<BinaryExpression> generators =
            [generator] +
            expressions.tail().findAll(this.&isAGeneratorExpression)

        // TODO Polymorphism doesn't seem to work with @CompileStatic
        Expression resultList = buildNestedLoops(variables, generators)

        // getVariables (only single value or list or closure)
        // getGuards (OPTIONAL FOR NOW)
        // getGenerator
        // getTake (OPTIONAL FOR NOW)
        // Generate new code

        return resultList
    }

    /**
     * [ i | i << (1..10) ]
     *
     **/
    Expression buildNestedLoops(
            VariableExpression variables,
            List<BinaryExpression> generators) {

        ListExpression generatedItem = listX(varX(variables.name))

        // TODO refactoring the inject expression to a method returning
        // a MethodCallExpression will make @CompileStatic pass
        MethodCallExpression expression =
            generators
            .reverse()
            .inject(generatedItem, this.&createInlineLoop)

        return expression

    }

    ListExpression listX(Expression... expressions) {
        return new ListExpression(expressions as List)
    }

    /**
     * [ { i + j } | i << (1..10), j << (1..10) ]
     *
     **/
    Expression buildNestedLoops(
            ClosureExpression variables,
            List<BinaryExpression> generators) {

        ListExpression generatedItem =
            listX(callX(variables, METHOD_NAME_DO_CALL,listX()))

        // TODO refactoring the inject expression to a method returning
        // a MethodCallExpression will make @CompileStatic pass
        MethodCallExpression expression =
            generators
            .reverse()
            .inject(generatedItem, this.&createInlineLoop)

        return expression

    }

    /**
     * [ [i, j] | i << (1..10), j << (1..10) ]
     *
     **/
    Expression buildNestedLoops(
            ListExpression variables,
            List<BinaryExpression> generators) {

        ListExpression generatedItem = listX(variables)

        MethodCallExpression expression =
            generators
            .reverse()
            .inject(generatedItem, this.&createInlineLoop)

        return expression
    }

    MethodCallExpression createInlineLoop(
        Expression previous,
        BinaryExpression next) {
        return callX(
            next.rightExpression,
            METHOD_NAME_COLLECT_MANY,
            closureX(
                params(param(make(Object),next.leftExpression.name)),
                block(stmt(previous))
            )
        )
    }

}
