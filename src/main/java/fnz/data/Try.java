package fnz.data;

/**
 *
 * @author mario
 * @param <A>
 */
public abstract class Try<A> implements Monad<A>, Or<A,Try<A>> {

    private final Type<A> typedRef;

    protected Try(Type<A> valueRef) {
        this.typedRef = valueRef;
    }

    public Type<A> getTypedRef() {
      return this.typedRef;
    }

    public abstract Boolean isSuccess();
    public abstract Boolean isFailure() ;
    public abstract Try<A> recover(Try<A> alternative);

    public static class Success<SUCCESS> extends Try<SUCCESS> {

        public Success(Type<SUCCESS> value) {
            super(value);
        }

        @Override
        public Boolean isSuccess() {
            return true;
        }

        @Override
        public Boolean isFailure() {
            return false;
        }

        @Override
        public <B, M extends Monad<B>> M bind(Function<SUCCESS, M> fn) {
             return fn.apply(getTypedRef().getValue());
        }

        @Override
        public <B> Applicative<B> fapply(Applicative<Function<SUCCESS, B>> afn) {
              return this.fmap(afn.getTypedRef().getValue());
        }

        @Override
        public <B, F extends Functor<B>> F fmap(Function<SUCCESS, B> fn) {
            try {
                return (F) success(fn.apply(getTypedRef().getValue()));
            } catch(Throwable th) {
                return (F) failure(getTypedRef(), th);
            }
        }

        @Override
        public Try<SUCCESS> recover(Try<SUCCESS> alternative) {
            return this;
        }

        @Override
        public Try<SUCCESS> or(Try<SUCCESS> alternative) {
            return this;
        }

        @Override
        public Try<SUCCESS> or(Function<SUCCESS, Try<SUCCESS>> alternative) {
            return this;
        }

    }

    public static class Failure<FAILURE> extends Try<FAILURE> {

        private final Throwable throwable;

        public Failure(Type<FAILURE> value, Throwable th) {
            super(value);
            this.throwable = th;
        }

        public Failure(Throwable throwable) {
            super(new Type<FAILURE>(null));
            this.throwable = throwable;
        }

        @Override
        public <B, M extends Monad<B>> M bind(Function<FAILURE, M> fn) {
          return (M) new Try.Failure<FAILURE>(throwable);
        }

        @Override
        public <B> Applicative<B> fapply(Applicative<Function<FAILURE, B>> afn) {
            return (Applicative<B>) new Try.Failure<FAILURE>(throwable);
        }

        @Override
        public <B, F extends Functor<B>> F fmap(Function<FAILURE, B> fn) {
            return (F) new Try.Failure<FAILURE>(throwable);
        }

        @Override
        public Boolean isFailure() {
            return true;
        }

        @Override
        public Boolean isSuccess() {
            return false;
        }

        public void throwException() throws Throwable {
            throw this.throwable;
        }

        public Throwable getException() {
             return this.throwable;
        }

        @Override
        public Try<FAILURE> recover(Try<FAILURE> alternative) {
            return alternative;
        }

        @Override
        public Try<FAILURE> or(Try<FAILURE> alternative) {
            return alternative;
        }

        @Override
        public Try<FAILURE> or(Function<FAILURE,Try<FAILURE>> alternative) {
            return alternative.apply(this.getTypedRef().getValue());
        }

    }

    public static <T> Try.Success<T> success(T value) {
        return new Try.Success(new Type(value));
    }

    public static <T> Try.Failure<T> failure(Type<T> value, Throwable th) {
        return new Try.Failure<T>(value, th);
    }

}
