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
        BinaryExpression generator = firstExpression.rightExpression

        List<BinaryExpression> generators =
            [generator] +
            expressions.tail().findAll(this.&isAGeneratorExpression)


        Expression resultList = buildNestedLoops(variables, generators)

        // getVariables (only single value or list or closure)
        // getGuards (OPTIONAL FOR NOW)
        // getGenerator
        // getTake (OPTIONAL FOR NOW)
        // Generate new code

//

        return resultList
    }

    /**
     * [ i | i << (1..10) ]
     *
     **/
    Expression buildNestedLoops(
            VariableExpression variables,
            List<BinaryExpression> generators) {

        return callX(
            listX(generators.first().rightExpression),
            METHOD_NAME_COLLECT_MANY,
            closureX(
                params(param(make(Object),variables.name)),
                block(stmt(varX(variables.name))))
        )

    }

    ListExpression listX(Expression... expressions) {
        return new ListExpression(Arrays.asList(expressions))
    }

    /**
     * [ { i + j } | i << (1..10), j << (1..10) ]
     *
     **/
    Expression buildNestedLoops(
            ClosureExpression variables,
            List<BinaryExpression> generators) {
        return null
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
                .inject(generatedItem) { previous, next ->
                    callX(
                        next.rightExpression,
                        METHOD_NAME_COLLECT_MANY,
                        closureX(
                            params(param(make(Object),next.leftExpression.name)),
                            block(stmt(previous))
                        )
                    )
                }

        return expression
    }


}
