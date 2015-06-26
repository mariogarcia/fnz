package fnz.data;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 *
 * @param <A> The contained type
 */
public abstract class Maybe<A> extends MonadType<A> implements Or<A,Maybe<A>> {

    public abstract boolean isPresent();

    public Maybe(Type<A> valueRef) {
        super(valueRef);
    }

    public static class Just<JUST> extends Maybe<JUST> {

        private Just(Type<JUST> valueRef) {
            super(valueRef);
        }

        // tag::fapply[]
        @Override
        public <B> Just<B> fapply(Applicative<Function<JUST, B>> afn) {
            return this.fmap(afn.getTypedRef().getValue());
        }
        // end::fapply[]

        // tag::functorspec2[]
        @Override
        public <B, F extends Functor<B>> F fmap(Function<JUST, B> fn) {
            return (F) just(fn.apply(this.getTypedRef().getValue()));
        }
        // end::functorspec2[]

        // tag::justbind[]
        @Override
        public <B, M extends Monad<B>> M bind(Function<JUST, M> fn) {
            return fn.apply(getTypedRef().getValue());
        }
        // end::justbind[]

        @Override
        public <B, M extends Monad<B>> M bind2(TypeAwareFunction<JUST, M> fn) {
            return fn.apply((Class<M>) this.getClass(), getTypedRef().getValue());
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public Maybe<JUST> or(Maybe<JUST> newOption) {
            return this;
        }

        @Override
        public Maybe<JUST> or(Function<JUST,Maybe<JUST>> newOption) {
            return this;
        }

        public static <A> Just<A> unit(A value) {
            return Maybe.just(value);
        }

        @Override
        public Boolean asBoolean() {
            return Boolean.TRUE;
        }

    }

    public static class Nothing<NOTHING> extends Maybe<NOTHING> {

        private Nothing() {
            super(new Type(null));
        }

        @Override
        public <B> Nothing<B> fapply(Applicative<Function<NOTHING, B>> afn) {
            return new Nothing();
        }


        @Override
        public <B, M extends Monad<B>> M bind(Function<NOTHING, M> fn) {
            return (M) new Nothing();
        }

        @Override
        public <B, M extends Monad<B>> M bind2(TypeAwareFunction<NOTHING, M> fn) {
            return (M) new Nothing();
        }

        // tag::nothingbind[]
        @Override
        public <B, F extends Functor<B>> F fmap(Function<NOTHING, B> fn) {
            return (F) new Nothing();
        }
        // end::nothingbind[]

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Maybe<NOTHING> or(Maybe<NOTHING> newOption) {
            return newOption;
        }

        @Override
        public Maybe<NOTHING> or(Function<NOTHING,Maybe<NOTHING>> newOption) {
            return newOption.apply(this.getTypedRef().getValue());
        }

        @Override
        public Boolean asBoolean() {
            return Boolean.FALSE;
        }

    }

    public static <T> Maybe.Just<T> just(T value) {
        return new Maybe.Just(new Type(value));
    }

    public static <T> Maybe.Nothing<T> nothing() {
        return new Maybe.Nothing();
    }

    public static <T> Maybe<T> maybe(final T value) {
        return (Maybe<T>) (DefaultGroovyMethods.asBoolean(DefaultGroovyMethods.collect(value)) ? just(value) : nothing());
    }

    public static <T,M extends Monad<T>> Maybe<T> maybe(M monad) {
        return maybe(monad.getTypedRef().getValue());
    }

}
