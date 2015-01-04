package fnz.control

import fnz.ast.MethodCallExpressionTransformer
import fnz.data.Fn
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.VariableScope

import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.SourceUnit

import groovyjarjarasm.asm.Opcodes

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
class TypeAstTransformer extends MethodCallExpressionTransformer implements Opcodes {

    static final String TYPE_METHOD_NAME = 'ftype'
    static final String FI_FUNCTION_NAME = 'apply'
    static final Expression MEANING_OF_LIFE = constX(42)

    TypeAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, TYPE_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        Expression firstArgumentExpression = firstArgumentExpressionFrom(methodCallExpression)

        if (isNotABinaryExpression(firstArgumentExpression)) {
            error(sourceUnit, firstArgumentExpression)

            return methodCallExpression
        }

        InnerClassNode innerClassNode =
            createFunctionalInterface((BinaryExpression) firstArgumentExpression)

        module.addClass(innerClassNode)

        return MEANING_OF_LIFE
    }

    private boolean isNotABinaryExpression(Expression expression) {
        return !(expression instanceof BinaryExpression)
    }

    private ModuleNode getModule() {
        return sourceUnit.AST
    }

    private Boolean byMainClassName(String mainName, ClassNode classNode) {
        return classNode.name == mainName
    }

    @CompileDynamic
    private InnerClassNode createFunctionalInterface(BinaryExpression fiExpression) {
        Expression typeInfo = fiExpression.leftExpression
        Expression fnInfo = fiExpression.rightExpression

        InnerClassNode innerClassNode = extractInnerClass(typeInfo)
        MethodNode abstractMethod = extractMethod(fnInfo)

        innerClassNode.addMethod(abstractMethod)

        return innerClassNode
    }

    private InnerClassNode extractInnerClass(MethodCallExpression methodCallExpression) {
        InnerClassNode innerClassNode = buildInnerClass(methodCallExpression.methodTarget.name)
        List<Expression> args = ((ArgumentListExpression) methodCallExpression.arguments).expressions
        Closure<GenericsType> toGeneric = { VariableExpression var -> new GenericsType(make(var.name)) }

        innerClassNode.setGenericsTypes(args.collect(toGeneric) as GenericsType[])

        return innerClassNode
    }

    private InnerClassNode extractInnerClass(VariableExpression typeInfoExpression) {
        return buildInnerClass(typeInfoExpression.name)
    }

    private InnerClassNode buildInnerClass(String innerClassName) {
        Closure<Boolean> search = this.&byMainClassName.curry(module.mainClassName)
        ClassNode outerClassNode = module.classes.find(search)

        InnerClassNode innerClassNode =
            new InnerClassNode(
                outerClassNode,
                innerClassName,
                innerClassModifiers,
                make(Object),
                [make(GroovyObject)] as ClassNode[],
                [] as MixinNode[]
            )

        return innerClassNode
    }

    private int getInnerClassModifiers() {
        return ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE
    }

    private Expression firstArgumentExpressionFrom(MethodCallExpression methodCallExpression) {
        ArgumentListExpression args = (ArgumentListExpression) methodCallExpression.arguments

        return args.first()
    }

    private void error(SourceUnit sourceUnit, ASTNode node) {
        sourceUnit
        .addError(
            new SyntaxException(
                "Expected binary expression here. Something like: Fn(A) >> String >> A",
                node.columnNumber,
                node.lineNumber
            )
        )
    }

    @CompileDynamic
    private MethodNode extractMethod(BinaryExpression fnExpression) {
        Expression inputExpression = fnExpression.leftExpression
        Expression outputExpression = fnExpression.rightExpression

        ClassNode returnType = extractReturnType(outputExpression)
        Parameter[] parameters = extractParametersFrom(inputExpression)
        ClassNode[] exceptions = [] as ClassNode[]

        MethodNode methodNode =
            new MethodNode(
                FI_FUNCTION_NAME,
                ACC_PUBLIC + ACC_ABSTRACT,
                returnType,
                parameters,
                exceptions,
                null
            )

        return methodNode
    }

    ClassNode extractReturnType(MethodCallExpression methodCallExpression) {
        return null
    }

    ClassNode extractReturnType(VariableExpression variableExpression) {
        return make(variableExpression.name)
    }

    Parameter[] extractParametersFrom(MethodCallExpression methodCallExpression) {
        return null
    }

    Parameter[] extractParametersFrom(VariableExpression variableExpression) {
        return params(
            param(make(variableExpression.name), 'input')
        )
    }

}
