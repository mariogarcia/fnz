package fnz.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation

/**
 * This class applies all transformers provided by the method
 * getTransformers in order to all class nodes present in
 * a given source unit instance.
 *
 * @since 1.0.3
 *
 */
abstract class AstExpressionTransformerAware extends AbstractASTTransformation {

    @SuppressWarnings('UnusedMethodParameter')
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
         List<ClassNode> classNodeList = sourceUnit.AST.classes.collect()

         classNodeList.each { ClassNode clazzNode ->
             transformers.each { Class clazz ->
                 clazz.newInstance(sourceUnit).visitClass(clazzNode)
             }
         }
    }

    abstract List<Class> getTransformers()

}
