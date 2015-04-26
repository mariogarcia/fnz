package fnz.flow

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.isCase
import static fnz.Fnz.Just

import groovy.transform.CompileStatic

import fnz.data.Maybe

/**
 * This control was inspired in Haskell's where clause.
 *
 * @author Mario Garcia
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

    private Where(final Map<?,?> parameters) {
        this.parameters = parameters
    }

    Where when(final Closure cl) {
        conditions << new Evaluation(condition:cl)
        return this
    }

    @SuppressWarnings('SpaceAroundMapEntryColon')
    Where when(final Class clazz) {
        conditions << new Evaluation(condition: { isCase(clazz, parameters.val) })
        return this
    }

    @SuppressWarnings('SpaceAroundMapEntryColon')
    Where when(final Collection collection) {
        conditions << new Evaluation(condition: { isCase(collection, parameters.val) })
        return this
    }

    @SuppressWarnings('SpaceAroundMapEntryColon')
    Where when(final Number number) {
        conditions << new Evaluation(condition: { isCase(number, parameters.val) })
        return this
    }

    Where then(final Closure cl) {
        conditions.last().execution = cl
        return this
    }

    @SuppressWarnings('SpaceAroundMapEntryColon')
    Where otherwise(final Closure cl) {
        conditions << new Evaluation(condition: { true }, execution:cl)
        return this
    }

    Where where (final Closure whereClause) {
        this.with(whereClause)
        return this
    }

    def propertyMissing(String name, value) {
        parameters.get(name, value)
    }

    def evaluate() {
        Closure<Evaluation> byApplyingParameters = applyDelegateToExecution.call(parameters)
        Closure execution =
            conditions.
                collect(byApplyingParameters).
                find(firstTrue).
                execution

        return execution.call()
    }

    private Closure<Closure<Evaluation>>  applyDelegateToExecution = { final parameters ->
       return { final Evaluation evaluation ->
            evaluation.execution.delegate = parameters
            evaluation.condition.delegate = parameters
            evaluation
       }
    }

    private Closure<Boolean>  firstTrue = { Evaluation evaluation ->
        evaluation.condition.call()
    }

    public static <T> Maybe<T> check(final Map values, final Closure<Evaluation> evaluation) {
        return internalCheck(values ?: [:], evaluation)
    }

    public static <T> Maybe<T> check(final Object value, final Closure<Evaluation> evaluation) {
        return internalCheck([val:value], evaluation)
    }

    private static <T> Maybe<T> internalCheck(final Object value, final Closure<Evaluation> evaluation) {
        def where = new Where((Map) value)
        where.with(evaluation)
        return Just(where.evaluate())
    }

}
