package fnz.control

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.AbstractASTTransformation

/**
 * This AST applies all transformations available in FNZ.
 *
 * @since 1.0.1
 *
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class FnzAst extends AbstractASTTransformation {

    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
         sourceUnit.AST.classes.each { ClassNode clazzNode ->
             new UnlessAstTransformer(sourceUnit).visitClass(clazzNode)
             new LetmAstTransformer(sourceUnit).visitClass(clazzNode)
             new LetAstTransformer(sourceUnit).visitClass(clazzNode)
         }
    }

}
