package fnz.control

import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.syntax.Token

import static org.codehaus.groovy.ast.tools.GeneralUtils.*
import static org.codehaus.groovy.ast.ClassHelper.make

import fnz.ast.ListExpressionTransformer

import groovy.transform.TailRecursive
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
        Expression generatedExpression = firstExpression.leftExpression
        BinaryExpression firstGenerator = asBinary(firstExpression.rightExpression)

        // TODO [].plus() is not recognized by @CompileStatic...yet
        List<BinaryExpression> generators =
            [firstGenerator] +
            expressions.tail().findAll(this.&isAGeneratorExpression)
        List<BinaryExpression> processedGenerators =
            processGenerators(generators)

        // TODO Polymorphism doesn't seem to work with @CompileStatic
        Expression resultList =
            buildNestedLoops(
                generatedExpression,
                processedGenerators)

        // getVariables (only single value or list or closure)
        // getGuards (OPTIONAL FOR NOW)
        // getGenerator
        // getTake (OPTIONAL FOR NOW)
        // Generate new code

        return resultList
    }

    @TailRecursive
    List<BinaryExpression> processGenerators(List<BinaryExpression> rawGenerators) {
        Boolean isThereAnyListComprehension = rawGenerators.any(this.&isListComprehensionPresent)

        if (!isThereAnyListComprehension) {
            return rawGenerators
        } else {
            return processGenerators(applyUnwrappingToList(rawGenerators))
        }
    }

    Boolean isListComprehensionPresent(BinaryExpression expression) {
        ListExpression expressionToCheck = expression.rightExpression
        Boolean isAListComprehension =
            isAPipeBinaryExpression(expressionToCheck.expressions.head())

        return isAListComprehension
    }

    List<BinaryExpression> applyUnwrappingToList(List<BinaryExpression> rawGenerators) {
        return rawGenerators.inject(new ArrayList<BinaryExpression>(), this.&applyUnwrappingToExpression)
    }

    List<BinaryExpression> applyUnwrappingToExpression(
        List<BinaryExpression> aggregation, BinaryExpression next) {

        if (next.any(this.&isListComprehensionPresent)) {
            aggregation.addAll(unwrap(next))
        } else {
            aggregation.add(next)
        }

        return aggregation
    }

    /**
     * This method unwraps a nested list comprehension and adds all its
     * elements in a list.
     *
     * Lets say we had:
     *
     * [ i | i << [ { x + y } | x << (1..10), y << (2..4) ]]
     *
     * We are receiving this binary expression:
     *
     * [ i << [ { x + y } | x << (1..10), y << (2..4)]]
     *
     * Right expression:
     *
     * [ { x + y } | x << (1..10), y << (2..4)]
     *
     * And left expression:
     *
     * i
     *
     * And we want to get this:
     *
     * [ x << (1..10), y << (2..4), i << { x + y }]
     *
     * If we get all binary expressions in order we could finally generate
     * the execution flow.
     *
     */
    List<BinaryExpression> unwrap(BinaryExpression containsListComprehension) {
        Expression originalVariable = containsListComprehension.leftExpression
        // Destructuring nested comprehension
        ListExpression list = containsListComprehension.rightExpression
        List<Expression> listExpressions = list.expressions.tail()
        BinaryExpression firstExpression = list.expressions.head()
        Expression listVariable = firstExpression.leftExpression
        Expression firstGenerator = firstExpression.rightExpression
        // This expression will link the current list with the nested one
        Expression linkExpression =
            new BinaryExpression(
                originalVariable,
                new Token(Types.LEFT_SHIFT,'<<',-1,-1),
                listVariable)
        // first inner generators and then the link between the current and
        // nested list
        List<Expression> resultList =
            ([firstGenerator] + listExpressions) << linkExpression

        return resultList // [ x << (1..10), y << (2..4), i << { x + y }]
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
