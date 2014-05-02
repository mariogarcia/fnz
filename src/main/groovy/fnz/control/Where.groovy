package fnz.control

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.find
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect

import org.codehaus.groovy.runtime.ComposedClosure
import groovy.transform.CompileStatic

import fnz.base.Option

/**
 * This control was inspired in Haskell's where clause.
 *
 * @marioggar
 * @since 0.1
 */
@CompileStatic
class Where {

    private static class Evaluation {
        Closure condition
        Closure execution
    }

    private List<Evaluation> conditions = []
    private Map<?, ?> parameters = [:]

    private Closure<Closure<Evaluation>>  applyDelegateToCondition = { final parameters ->
        return { final Evaluation evaluation ->
            evaluation.condition.delegate = parameters
            evaluation
        }
    }

    private Closure<Closure<Evaluation>>  applyDelegateToExecution = { final parameters ->
       return { final Evaluation evaluation ->
            evaluation.execution.delegate = parameters
            evaluation
       }
    }

    private Closure<Boolean>  firstTrue = { Evaluation evaluation ->
        evaluation.condition.call()
    }

    def when(Closure cl) {
        def createCondition = { Closure closure -> new Evaluation(condition: closure)}
        conditions << createCondition(cl)
        return this
    }

    def rightShift(Closure value) {
        then(value)
    }

    def then(Closure cl) {
        conditions.last().execution = cl
        return this
    }

    def otherwise(Closure cl) {
        conditions << new Evaluation(condition: {true}, execution: cl)
    }

    def where (Closure whereClause) {
        this.with(whereClause)
        evaluate()
    }

    def propertyMissing(String name, value) {
        parameters[name] = value
    }

    def evaluate() {
        Closure<Evaluation> byApplyingParameters = composedClosure.call(parameters)
        Closure execution = find(collect(conditions, byApplyingParameters), firstTrue).execution

        return execution.call()
    }

    private ComposedClosure<Closure<Evaluation>> getComposedClosure() {
       return new ComposedClosure(applyDelegateToCondition, applyDelegateToExecution)
    }

    public static <T> Option<T> check(Map values, Closure<Evaluation> evaluation) {
        def where = new Where()
        where.parameters = values
        return Option.of(where.with(evaluation))
    }

}
