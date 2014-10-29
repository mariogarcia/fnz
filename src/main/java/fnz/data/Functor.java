package fnz.data;

/**
 *
 * @param <A>
 */
public interface Functor<A> {
    public <B, F extends Functor<B>> F fmap(Function<A,B> fn);
}
