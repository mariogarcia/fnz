package fnz.data;

/**
 *
 * @param <A>
 */
public abstract class Maybe<A> implements Monad<A>, Or<A,Maybe<A>> {

    private final Type<A> typedRef;

    protected Maybe(Type<A> valueRef) {
        this.typedRef = valueRef;
    }

    @Override
    public Type<A> getTypedRef() {
        return this.typedRef;
    }

    public abstract boolean isPresent();

    public abstract Maybe<A> or(Maybe<A> newOption);

    public static class Just<JUST> extends Maybe<JUST> {

        public Just(Type<JUST> valueRef) {
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

    }

    public static class Nothing<NOTHING> extends Maybe<NOTHING> {

        public Nothing() {
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

    }

    public static <T> Maybe.Just<T> just(T value) {
        return new Maybe.Just(new Type(value));
    }

    public static <T> Maybe.Nothing<T> nothing() {
        return new Maybe.Nothing();
    }

}
