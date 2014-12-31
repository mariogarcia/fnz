package fnz.ast

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer

/**
 * Most transformers need at some point the source unit in order to fix or apply
 * properly the scope to each variable.
 *
 * This class enforces the use of a SourceUnit instance for every transformer
 *
 * @author Mario Garcia
 *
 */
class DefaultClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {

    private SourceUnit sourceUnit

    /**
     * This constructor needs a source unit
     *
     * @param sourceUnit the related source unit where the expression belongs
     */
    DefaultClassCodeExpressionTransformer(SourceUnit sourceUnit) {
          this.sourceUnit = sourceUnit
    }

    /**
     * This method returns the source unit
     *
     * @return the source unit related to the expression we want to transform
     */
    SourceUnit getSourceUnit() {
          return this.sourceUnit
    }
}
