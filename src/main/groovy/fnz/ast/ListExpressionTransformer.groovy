package fnz.ast

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.SourceUnit

import fnz.ast.DefaultClassCodeExpressionTransformer

/**
 * This class can be used as a base to transform certain ListExpression instances.
 *
 * Let's say we had a expression like a list comprehension:
 * <pre>[ i | i << 1..10, { i > 5 }]</pre>
 * We can transform that expression into another creating an instance of a ListExpression:
 * <pre>
 * class ListComprehensionTransformer extends ListExpressionTransformer {
 *       ListComprehensionTransformer(SourceUnit sourceUnit) {
 *           super(sourceUnit)
 *       }
 *
 *       Expression transformListExpression(ListExpression listExpression) {
 *           // implement the transformation
 *       }
 * }
 * </pre>
 * @author Mario Garcia
 */
@CompileStatic
abstract class ListExpressionTransformer extends DefaultClassCodeExpressionTransformer {

    /**
     * Every instance needs the source unit awareness
     *
     * @param sourceUnit Needed to apply scope
     */
    ListExpressionTransformer(SourceUnit sourceUnit) {
         super(sourceUnit)
    }

    @Override
    Expression transform(Expression expression) {
        if (!expression) return

        Boolean isTheList  =
            expression instanceof ListExpression &&
            isThisListEligible(expression)

        return isTheList ?
        this.transformListExpression(expression) :
        expression.transformExpression(this)
    }

    /**
     * This method checks whether the list expression needs to be
     * transformed or not
     *
     * @param listExpression the checked expression
     * @return true if the expression needs to be changed, or false otherwise
     **/
    abstract Boolean isThisListEligible(ListExpression listExpression)

    /**
     * This method will transform the expression into its final version.
     *
     * @param listExpression the list expression you want to change
     * @return the final version of the expression
     */
    abstract Expression transformListExpression(ListExpression listExpression)

}
