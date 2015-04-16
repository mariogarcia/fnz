package fnz.control

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import fnz.ast.AstExpressionTransformerAware

/**
 * This AST applies all transformations available in FNZ.
 *
 * @since 1.0.1
 *
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class FnzAst extends AstExpressionTransformerAware {

    List<Class> getTransformers() {
        return [
            UnlessAstTransformer,
            LetmAstTransformer,
            LetAstTransformer,
            TypeAstTransformer
        ]
    }

}
