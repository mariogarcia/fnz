package fnz.ast

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.syntax.SyntaxException

/**
 * Most transformers need at some point the source unit in order to fix or apply
 * properly the scope to each variable.
 *
 * This class enforces the use of a SourceUnit instance for every transformer
 *
 * @author Mario Garcia
 *
 */
abstract class DefaultClassCodeExpressionTransformer
    extends ClassCodeExpressionTransformer implements Opcodes {

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

    /**
     * This method creates a new SyntaxException
     *
     * @param node The node causing the exception
     * @param message A meaningful exception message to the user
     */
    void addError(ASTNode node, String message) {
        sourceUnit.addError(
            new SyntaxException(
                message,
                node.columnNumber,
                node.lineNumber
            )
        )
    }

    /**
     * This method returns the module of the current
     * SourceUnit instance
     *
     * @return a ModuleNode instance
     */
    ModuleNode getModule() {
        return sourceUnit.AST
    }

    /**
     * Sometimes could be useful to get the package name
     * of the current module
     *
     * @return A String representing the current qualified package name
     */
    String getModulePackageName() {
        return module?.packageName?.with { "$it" } ?: ''
    }

    /**
     * Classes implementing this method should provide a way
     * of knowing whether the expression should be transform
     * or not
     *
     * @param expression the expression we may want to transform
     * @return true if the current expression should be transformed or
     * false otherwise
     */
    abstract Boolean isTransformable(Expression expression)

}
