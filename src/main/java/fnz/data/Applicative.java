package fnz.data;

/**
 *
 * @param <A>
 */
public interface Applicative<A> extends Functor<A> {
    public <U extends Type<A>> U getTypedRef();
    public <B> Applicative<B> fapply(Applicative<Function<A,B>> afn);
}
