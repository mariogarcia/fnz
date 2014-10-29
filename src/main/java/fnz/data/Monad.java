package fnz.data;

/**
 *
 * @param <A>
 */
public interface Monad<A> extends Applicative<A> {
    public <B,M extends Monad<B>> M bind(Function<A,M> fn);
}
