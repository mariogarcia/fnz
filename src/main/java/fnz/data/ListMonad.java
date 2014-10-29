package fnz.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @param <A>
 */
public class ListMonad<A> implements Monad<A> {

    private final List<A> value;

    public ListMonad(List<A> values) {
        this.value = values;
    }

    @Override
    public <B, M extends Monad<B>> M bind(Function<A, M> fn) {
        if (isNullOrEmpty())
            return (M) this;

        List<B> items = new ArrayList<>();
        for (A a : this.value) {
            ListMonad<B> transformed = (ListMonad<B>) fn.apply(a);
            items.addAll(transformed.getTypedRef().getValue());
        }
        return (M) list(items);
    }

    @Override
    public TypeCollection<A> getTypedRef() {
        return new TypeCollection(this.value);
    }

    @Override
    public <B> Applicative<B> fapply(Applicative<Function<A, B>> afn) {
        if (isNullOrEmpty())
            return (Applicative) this;
        return this.fmap(afn.getTypedRef().getValue());
    }

    @Override
    public <B, F extends Functor<B>> F fmap(Function<A, B> fn) {
        if (isNullOrEmpty())
            return (F) this;
        List<B> transformed = new ArrayList<>();
        for (A v : this.getTypedRef().getValue()) {
            transformed.add(fn.apply(v));
        }
        return (F) new ListMonad<>(transformed);
    }

    public boolean isNullOrEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    public static <T> ListMonad<T> list(T... values){
        return list(Arrays.asList(values));
    }

    public static <T> ListMonad <T> list(List<T> values){
        return new ListMonad<>(values);
    }

}
