package fnz.control;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import org.codehaus.groovy.runtime.ComposedClosure;

/**
 * This control was inspired by Haskell's where control type.
 *
 * @author @marioggar
 */
public class Where extends GroovyObjectSupport {

    /*
     * Where clauses are composed of several evaluation clauses. Each
     * evaluation is like a when->then construction
     */
    private static class Evaluation {

        Closure<Boolean> when;
        Closure<?> then;

        private Evaluation() {}

        public static Evaluation of(Closure<Boolean> when, Closure<?> then) {
            Evaluation evaluation = new Evaluation();
            evaluation.when = when;
            evaluation.then = then;

            return evaluation;
        }

    }

    private final LinkedList<Evaluation> conditionList = new LinkedList<Evaluation>();
    private final Map<Object,Object> parameters;

    public Where(Map<Object,Object> initialMap) {
        super();
        this.parameters = initialMap;
    }

    public Object propertyMissing(String name, Object value) {
        parameters.put(name, value);
        return parameters;
    }

    /**
     * This method executes the where logic and return the
     * evaluation result
     *
     * @param body The where body clause containing evaluation blocks
     */
    public Object where(Closure<Object> whereClause) {
        whereClause.setDelegate(this);
        whereClause.call();

        return this.evaluate();
    }

    /**
     *
     */
    public Where when(Closure<Boolean> whenClause) {
        conditionList.add(Evaluation.of(whenClause, null));

        return this;
    }

    /**
     *
     */
    public Where then(Closure<?> thenClause) {
        conditionList.getLast().then = thenClause;

        return this;
    }

    /**
     *
     */
    public Where otherwise(Closure<?> thenClause) {
        conditionList.add(Evaluation.of(null, thenClause));

        return this;
    }

    /**
     *
     */
    public Where rightShift(Closure<?> value) {
        then(value);

        return this;
    }

    public static Object check(Map<Object,Object> initValues, Closure<?> evaluation) {
        Where where = new Where(initValues);

        evaluation.setDelegate(where);
        return evaluation.call();
    }

    private Closure<Closure<Evaluation>> applyDelegateToExecution() {
        return new Closure<Closure<Evaluation>>(null) {
            public Closure<Evaluation> call(Object... parameterArgs) {
                return new Closure<Evaluation>(null) {
                    public Evaluation call(Object... evaluationArgs) {
                        final Evaluation evaluation = (Evaluation) evaluationArgs[0];
                        evaluation.then.setDelegate(parameters);
                        return evaluation;
                    }
                };
            }
        };
    }

    private Closure<Closure<Evaluation>> applyDelegateToCondition() {
        return new Closure<Closure<Evaluation>>(null) {
            public Closure<Evaluation> call(Object... parameterArgs) {
                return new Closure<Evaluation>(null) {
                    public Evaluation call(Object... evaluationArgs) {
                        final Evaluation evaluation = (Evaluation) evaluationArgs[0];
                        evaluation.when.setDelegate(parameters);
                        return evaluation;
                    }
                };
            }
        };
    }

    /**
     *
     */
    private Object evaluate() {
        return find(collect(conditionList, getComposedClosure()), getFirstTrueClosure()).call();
    }

    private Closure<Evaluation> getComposedClosure() {
        return new ComposedClosure<Closure<Evaluation>>(applyDelegateToCondition(), applyDelegateToExecution()).call(parameters);
    }

    private Closure<Boolean> getFirstTrueClosure() {
        return new Closure<Boolean> (null) {
            public Boolean call(Object... something) {
                return ((Evaluation) something[0]).when.call();
            }
        };
    }

    private Closure<?> find(List<Evaluation> evaluationList, Closure<Boolean> filter) {
        List<Evaluation> processedEvaluationList = new LinkedList<Evaluation>();
        for(Evaluation evaluation : evaluationList) {
            if (filter.call(evaluation)) {
                return evaluation.then;
            }
        }
        return null;
    }

    private List<Evaluation> collect(List<Evaluation> conditions, Closure<Evaluation> transformer) {
        List<Evaluation> processedEvaluationList = new LinkedList<Evaluation>();
        for(Evaluation evaluation : conditions) {
            processedEvaluationList.add(transformer.call(evaluation));
        }
        return processedEvaluationList;
    }

}
