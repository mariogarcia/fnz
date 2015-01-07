package fnz.control

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * This AST applies all transformations available in FNZ.
 *
 * @since 1.0.1
 *
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class FnzAst extends AbstractASTTransformation {

    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
         List<ClassNode> classNodeList = sourceUnit.AST.classes.collect()

         classNodeList.each { ClassNode clazzNode ->
             new UnlessAstTransformer(sourceUnit).visitClass(clazzNode)
             new LetmAstTransformer(sourceUnit).visitClass(clazzNode)
             new LetAstTransformer(sourceUnit).visitClass(clazzNode)
             new TypeAstTransformer(sourceUnit).visitClass(clazzNode)
         }
    }

}
