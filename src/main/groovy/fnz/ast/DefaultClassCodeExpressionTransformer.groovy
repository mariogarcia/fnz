package fnz.ast

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer

class DefaultClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {

    private SourceUnit sourceUnit

    DefaultClassCodeExpressionTransformer(SourceUnit sourceUnit) {
          this.sourceUnit = sourceUnit
    }

    SourceUnit getSourceUnit() {
          return this.sourceUnit
    }
}
