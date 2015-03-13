package fnz.data;

/**
 *
 * @param <A>
 */
public abstract class Either<A> implements Monad<A>, Or<A,Either<A>> {

    private final Type<A> typedRef;

    protected Either(Type<A> value) {
        this.typedRef = value;
    }

    @Override
    public Type<A> getTypedRef() {
        return this.typedRef;
    }

    public abstract boolean isLeft();
    public abstract boolean isRight();

    public static class Right<R> extends Either<R> {

        private Right(Type<R> valueRef) {
            super(valueRef);
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public <B> Right<B> fapply(Applicative<Function<R, B>> afn) {
            return this.fmap(afn.getTypedRef().getValue());
        }

        @Override
        public <B, M extends Monad<B>> M bind(Function<R, M> fn) {
            return fn.apply(getTypedRef().getValue());
        }

        @Override
        public <B, F extends Functor<B>> F fmap(Function<R, B> fn) {
            return (F) right(fn.apply(getTypedRef().getValue()));
        }

        @Override
        public Either<R> or(Either<R> newOption) {
            return this;
        }

        @Override
        public Either<R> or(Function<R,Either<R>> newOption) {
            return this;
        }

    }

    public static class Left<L> extends Either<L> {

        private Left(Type<L> valueRef) {
            super(valueRef);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public <B> Left<B> fapply(Applicative<Function<L, B>> afn) {
            return new Left(getTypedRef());
        }

        @Override
        public <B, M extends Monad<B>> M bind(Function<L, M> fn) {
            return (M) new Left(getTypedRef());
        }

        @Override
        public <B, F extends Functor<B>> F fmap(Function<L, B> fn) {
            return (F) new Left(getTypedRef());
        }

        @Override
        public Either<L> or(Either<L> newOption) {
            return newOption;
        }

        @Override
        public Either<L> or(Function<L,Either<L>> newOption) {
            return newOption.apply(this.getTypedRef().getValue());
        }

    }

    public static <T> Right<T> right(T value) {
        return new Right(new Type(value));
    }

    public static <T> Left<T> left(T value) {
        return new Left(new Type(value));
    }

}
