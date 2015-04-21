package fnz.ast.type

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params

import static fnz.ast.AstUtils.getArgs
import static fnz.ast.AstUtils.isBinaryExpression
import static fnz.ast.AstUtils.getUniqueIdentifier
import static fnz.ast.AstUtils.isToken

import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL

import fnz.ast.MethodCallExpressionTransformer

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.GenericsType

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression

import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.control.SourceUnit

import fnz.ast.AstUtils

@CompileStatic
class TypeAstTransformer extends MethodCallExpressionTransformer {

    static final String TYPE_METHOD_NAME = 'ftype'
    static final String FI_FUNCTION_NAME = 'apply'
    static final String FI_FUNCTION_PARAM_NAME = 'input'
    static final Expression MEANING_OF_LIFE = constX(42)

    TypeAstTransformer(SourceUnit sourceUnit) {
        super(sourceUnit, TYPE_METHOD_NAME)
    }

    Expression transformMethodCall(final MethodCallExpression methodCallExpression) {
        Expression firstArg = getArgs(methodCallExpression).first()
        Boolean isValid = checkIsBinaryExpressionWithToken(firstArg, COMPARE_GREATER_THAN_EQUAL)

        if (!isValid) return

        InnerClassNode innerClassNode = getFunctionalInterface((BinaryExpression) firstArg)
        module.addClass(innerClassNode)

        return MEANING_OF_LIFE
    }

    @CompileDynamic
    Boolean checkIsBinaryExpressionWithToken(Expression expression, int tokenReference) {
        if (!isBinaryExpression(expression)) {
            addError(expression, "Expected binary expression here. Something like: Fn(A) >> String >> A")
            return false
        }

        if (!isToken(expression.operation, tokenReference)) {
            addError(expression, "Token expected $tokenReference got ${expression.operation}")
            return false
        }

        return true
    }

    private Boolean byMainClassName(String mainName, ClassNode classNode) {
        return classNode.name == mainName
    }

    @CompileDynamic
    private InnerClassNode getFunctionalInterface(BinaryExpression fiExpression) {
        Expression typeInfo = fiExpression.leftExpression
        Expression fnInfo = fiExpression.rightExpression

        InnerClassNode innerClassNode = extractInnerClass(typeInfo)
        MethodNode abstractMethod = extractMethod(fnInfo)

        innerClassNode.addMethod(abstractMethod)

        return innerClassNode
    }

    private InnerClassNode extractInnerClass(MethodCallExpression methodCallExpression) {
        String innerClassName = "${modulePackageName}${methodCallExpression.methodAsString}"
        InnerClassNode innerClassNode = getInnerClass(innerClassName)

        innerClassNode.setGenericsTypes(extractGenericsFrom(methodCallExpression))
        innerClassNode.genericsPlaceHolder = true

        return innerClassNode
    }

    private InnerClassNode extractInnerClass(VariableExpression typeInfoExpression) {
        return getInnerClass("${modulePackageName}${typeInfoExpression.name}")
    }

    private InnerClassNode getInnerClass(String innerClassName) {
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
        return extractTypeFrom(methodCallExpression)
    }

    private ClassNode extractReturnType(VariableExpression variableExpression) {
        return make(variableExpression.name)
    }

    private Parameter[] extractParametersFrom(MethodCallExpression methodCallExpression) {
        return params(getParameterFrom(methodCallExpression))
    }

    private Parameter[] extractParametersFrom(VariableExpression variableExpression) {
        return params(getParameterFrom(variableExpression))
    }

    private Parameter[] extractParametersFrom(ListExpression listExpression) {
        return listExpression.expressions.collect(this.&getParameterFrom) as Parameter[]
    }

    private Parameter getParameterFrom(VariableExpression variable) {
        return param(make(variable.name), getUniqueIdentifier())
    }

    private Parameter getParameterFrom(MethodCallExpression methodCallExpression) {
        return param(extractTypeFrom(methodCallExpression), getUniqueIdentifier())
    }

    private ClassNode extractTypeFrom(MethodCallExpression methodCallExpression) {
        String principalClassName = methodCallExpression.methodAsString

        ClassNode classNode = make(principalClassName)
        classNode.genericsPlaceHolder = true
        classNode.genericsTypes = extractGenericsFrom(methodCallExpression)

        return classNode
    }

    private GenericsType[] extractGenericsFrom(MethodCallExpression methodCallExpression) {
        ArgumentListExpression args = ((ArgumentListExpression) methodCallExpression.arguments)
        List<Expression> genericTypes = args.expressions
        GenericsType[] genericsArray =
            genericTypes.collect(this.&extractGenericsTypeFrom) as GenericsType[]

        return genericsArray
    }

    private GenericsType extractGenericsTypeFrom(VariableExpression variableExpression) {
        return new GenericsType(make(variableExpression.name))
    }

}
