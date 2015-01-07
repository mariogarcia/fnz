package fnz.control

import fnz.data.Try
import fnz.data.Maybe
import fnz.data.Function
import fnz.data.ListMonad
import fnz.ast.MethodCallExpressionTransformer

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException

import static fnz.data.Fn.*
import static org.codehaus.groovy.control.ResolveVisitor.DEFAULT_IMPORTS
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
class TypeAstTransformer extends MethodCallExpressionTransformer implements Opcodes {

    static final String TYPE_METHOD_NAME = 'ftype'
    static final String FI_FUNCTION_NAME = 'apply'
    static final Expression MEANING_OF_LIFE = constX(42)

    private Resolver resolver = new Resolver()

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

    private String getModulePackageName() {
        return module?.packageName?.with { "$it" } ?: ''
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
        String innerClassName = "${modulePackageName}${methodCallExpression.methodTarget.name}"
        InnerClassNode innerClassNode = buildInnerClass(innerClassName)
        List<Expression> args = ((ArgumentListExpression) methodCallExpression.arguments).expressions
        Closure<GenericsType> toGeneric = { VariableExpression var -> new GenericsType(make(var.name)) }

        innerClassNode.setGenericsTypes(args.collect(toGeneric) as GenericsType[])

        return innerClassNode
    }

    private InnerClassNode extractInnerClass(VariableExpression typeInfoExpression) {
        return buildInnerClass("${modulePackageName}${typeInfoExpression.name}")
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
                [] as ClassNode[],
                [] as MixinNode[]
            )

        return innerClassNode
    }

    private int getInnerClassModifiers() {
        return ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE
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
                ACC_PUBLIC | ACC_ABSTRACT,
                returnType,
                parameters,
                exceptions,
                null
            )

        return methodNode
    }

    private ClassNode extractReturnType(MethodCallExpression methodCallExpression) {
        return null
    }

    private ClassNode extractReturnType(VariableExpression variableExpression) {
        return resolver.resolve(variableExpression.name)
    }

    private Parameter[] extractParametersFrom(MethodCallExpression methodCallExpression) {
        return null
    }

    private Parameter[] extractParametersFrom(VariableExpression variableExpression) {
        return params(
            param(resolver.resolve(variableExpression.name), 'input')
        )
    }

    @CompileDynamic
    class Resolver {

        Function<String,Try> classFor = recover(
            { String className -> make(Class.forName(className)) },
            { String className -> null }
        )

        ClassNode resolve(final String name) {
            return DEFAULT_IMPORTS.findResult { pkg ->
                val(Just("$pkg$name").bind(classFor))
            }
        }

    }

}
