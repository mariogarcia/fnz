package fnz.data;

/**
 *
 * @param <A>
 */
public interface Monad<A> extends Applicative<A> {

    public <B,M extends Monad<B>> M bind(Function<A,M> fn);

    /**
     * The behavior is the same as the bind(Function<A,M>) method but with type awareness.
     * <br/>
     * This method was created exclusively for type inference for the $do/$return expressions.
     * For instance in an expression like the following:
     * <br/>
     * <pre>
     *     $do {
     *         x = Just(1)
     *         y = Just(x + 1)
     *
     *         $return x + y
     *     }
     * </pre>
     * <br/>
     * <br/>
     * The expression is translated into this:
     * <br/>
     * <pre>
     *     Just(1).bind { _clazz1231, x ->
     *         Just(x + 1).bind { _clazz2324, y ->
     *             _clazz2324.unit(y)
     *         }
     *     }
     * </pre>
     * <br/>
     * That way we don't have to specify the type of the Monad needed to
     * end the expression, the function itself carries the type.
     *
     * @param fn a function aware of the type of the returned monad
     * @return a monad as a result of the function execution
     *
     */
    public <B,M extends Monad<B>> M bind2(TypeAwareFunction<A,M> fn);

    /**
     * This method is a helper to return the real value wrapped in
     * this container
     *
     * @return an instance of T
     */
    public <U> U get();

}
