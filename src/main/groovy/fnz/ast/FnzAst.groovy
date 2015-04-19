package fnz.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation

import fnz.ast.flow.UnlessAstTransformer
import fnz.ast.flow.LetmAstTransformer
import fnz.ast.flow.LetAstTransformer

import fnz.ast.type.TypeAstTransformer

/**
 * This AST applies all transformations available in FNZ.
 *
 * @since 1.0.1
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class FnzAst extends AstExpressionTransformerAware {

    @Override
    List<Class> getTransformers() {
        return [
            UnlessAstTransformer,
            LetmAstTransformer,
            LetAstTransformer,
            TypeAstTransformer
        ]
    }

}
