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

        if (isAnEmptyListExpression(expressions)){
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

    Boolean isAListExpression(Expression expression) {
        return expression instanceof ListExpression
    }

    BinaryExpression asBinary(Expression expression) {
        return (BinaryExpression) expression
    }

    @Override
    Expression transformListExpression(ListExpression listExpression) {
        // All expressions
        List<Expression> expressions = listExpression.expressions

        // First expression
        BinaryExpression firstExpression = asBinary(expressions.head())
        BinaryExpression firstGenerator = asBinary(firstExpression.rightExpression)
        Expression generatedExpression = firstExpression.leftExpression
        List<BinaryExpression> tailGenerators = expressions.tail().findAll(this.&isAGeneratorExpression)

        // Getting and maybe processing generators
        List<BinaryExpression> generators = pushToHead(tailGenerators, firstGenerator)
        List<BinaryExpression> processedGenerators = processGenerators(generators)

        // Processing all binary expressions representing the whole list comp.
        Expression resultList =
            buildNestedLoops(generatedExpression, processedGenerators)

        // getGuards (OPTIONAL FOR NOW)
        // getTake (OPTIONAL FOR NOW)

        return resultList
    }

    static <U> List<U> pushToHead(List<U> list, U element) {
        return [element] + list
    }

    /**
     * List comprehensions may have nested expressions. That should be
     * possible. So we need to make sure all nested operations are aligned.
     *
     * This method goes through all binary expressions and those
     * which are list comprehensions are split into simpler binary
     * expressions.
     *
     * The recursive look up has been minimized to this method in order
     * to allow a tail-recursive invokation.
     *
     * @param rawGenerators generators of the original list comprehension
     * which may be a list comprehensions theirselves.
     * @return a list of binary expressions representing all possible
     * nested list comprehensions.
     */
    @TailRecursive
    List<BinaryExpression> processGenerators(List<BinaryExpression> rawGenerators) {
        Boolean isThereAnyListComprehension = rawGenerators.any(this.&isListComprehensionPresent)

        if (!isThereAnyListComprehension) {
            return rawGenerators
        } else {
            return processGenerators(applyUnwrappingToList(rawGenerators))
        }
    }

    /**
     * This method checks whether the binary expression passed as parameter
     * contains a list comprehension or not.
     *
     * @param expression the checked expression
     * @return true if the binary expression contains a list expression
     * false otherwise
     */
    Boolean isListComprehensionPresent(BinaryExpression expression) {
        Expression expressionToCheck = expression.rightExpression

        if (!isAListExpression(expressionToCheck)) {
            return false
        }

        Boolean isAListComprehension =
            isAPipeBinaryExpression(expressionToCheck.expressions.head())

        return isAListComprehension
    }

    /**
     * This method applies the unwrap method to all binary expressions in the list
     * passed as parameter
     *
     * @param rawGenerators
     * @return unwrapped binary expressions
     *
     */
    List<BinaryExpression> applyUnwrappingToList(List<BinaryExpression> rawGenerators) {
        return rawGenerators.inject(new ArrayList<BinaryExpression>(), this.&applyUnwrappingToExpression)
    }

    /**
     * When a list comprehension has been found, then there should be
     * one or more binary expressions representing the generators of that
     * list, if it is not a list comprehension, then it only will have a
     * unique binary expression.
     *
     * @param aggregation The list into which all binary expressions will be
     * added to build the final representation of the list comprehension
     * @param next The next binary expression to check
     */
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
                treatExpression(listVariable))
        // first inner generators and then the link between the current and
        // nested list
        List<Expression> resultList =
            ([firstGenerator] + listExpressions) << linkExpression

        return resultList // [ x << (1..10), y << (2..4), i << { x + y }]
    }


    Expression buildNestedLoops(
            Expression variable,
            List<BinaryExpression> generators) {

        MethodCallExpression expression =
            generators
            .reverse()
            .inject(treatExpression(variable), this.&createInlineLoop)

        return expression

    }

    /**
     * This method has been added here Because there is not a listX()
     * method in Groovy's GeneralUtils class
     *
     * This method gets any number of expressions and wrap them into
     * a list expression
     *
     * @param expressions
     * @return a list expression containing all expressions passed
     * as parameters
     */
    ListExpression listX(Expression... expressions) {
        return new ListExpression(expressions as List)
    }

    /**
     * This method nests method calls in order to create expressions
     * of type:
     *
     * (1..2).collectMany { x ->
     *     (2..4).collectMany { y ->
     *         [ [x,y] ]
     *     }
     * }
     *
     * @param previous the nested expression
     * @param next the binary expression we want to create a method call from
     * @return a nested method call expression
     *
     */
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

    /**
     * [ i | i << (1..10) ]
     *
     **/
    ListExpression treatExpression(VariableExpression expression) {
        return listX(varX(expression.name))
    }

    /**
     * [ { i + j } | i << (1..10), j << (1..10) ]
     *
     **/
    ListExpression treatExpression(ClosureExpression expression) {
        return  listX(callX(expression, METHOD_NAME_DO_CALL,listX()))
    }

    /**
     * [ [i, j] | i << (1..10), j << (1..10) ]
     *
     **/
    ListExpression treatExpression(ListExpression expression) {
        return listX(expression)
    }

}
