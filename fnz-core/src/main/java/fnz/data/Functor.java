package fnz.data;

/**
 *
 * @param <A> The contained type
 */
public interface Functor<A> {
    public <B, F extends Functor<B>> F fmap(Function<A,B> fn);
}
