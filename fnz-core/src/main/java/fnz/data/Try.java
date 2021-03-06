package fnz.data;

/**
 *
 * @param <A> The contained type
 */
public abstract class Try<A> extends MonadType<A> implements Or<A,Try<A>> {

    protected Try(Type<A> valueRef) {
        super(valueRef);
    }

    public abstract Boolean isSuccess();
    public abstract Boolean isFailure() ;

    public static class Success<SUCCESS> extends Try<SUCCESS> {

        private Success(Type<SUCCESS> value) {
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
        public <B, M extends Monad<B>> M bind2(TypeAwareFunction<SUCCESS, M> fn) {
            return fn.apply((Class<M>) this.getClass(), getTypedRef().getValue());
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
        public Try<SUCCESS> or(Try<SUCCESS> alternative) {
            return this;
        }

        @Override
        public Try<SUCCESS> or(Function<SUCCESS, Try<SUCCESS>> alternative) {
            return this;
        }

        public static <A> Success<A> unit(A value) {
            return Try.success(value);
        }

        @Override
        public Boolean asBoolean() {
            return Boolean.TRUE;
        }

    }

    public static class Failure<FAILURE> extends Try<FAILURE> {

        private final Throwable throwable;

        private Failure(Type<FAILURE> value, Throwable th) {
            super(value);
            this.throwable = th;
        }

        private Failure(Throwable throwable) {
            super(new Type<FAILURE>(null));
            this.throwable = throwable;
        }

        @Override
        public <B, M extends Monad<B>> M bind(Function<FAILURE, M> fn) {
            return (M) new Try.Failure<FAILURE>(getTypedRef(), throwable);
        }

        @Override
        public <B, M extends Monad<B>> M bind2(TypeAwareFunction<FAILURE, M> fn) {
            return (M) new Try.Failure<FAILURE>(getTypedRef(), throwable);
        }

        @Override
        public <B> Applicative<B> fapply(Applicative<Function<FAILURE, B>> afn) {
            return (Applicative<B>) new Try.Failure<FAILURE>(getTypedRef(), throwable);
        }

        @Override
        public <B, F extends Functor<B>> F fmap(Function<FAILURE, B> fn) {
            return (F) new Try.Failure<FAILURE>(getTypedRef(), throwable);
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
        public Try<FAILURE> or(Try<FAILURE> alternative) {
            return alternative;
        }

        @Override
        public Try<FAILURE> or(Function<FAILURE,Try<FAILURE>> alternative) {
            return alternative.apply(this.getTypedRef().getValue());
        }

        @Override
        public Boolean asBoolean() {
            return Boolean.FALSE;
        }

    }

    public static <T> Try.Success<T> success(T value) {
        return new Try.Success(new Type(value));
    }

    public static <T> Try.Failure<T> failure(T value) {
        return failure(value, new IllegalArgumentException("" + value));
    }

    public static <T> Try.Failure<T> failure(T value, Throwable th) {
        return failure(new Type<T>(value), th);
    }

    public static <T> Try.Failure<T> failure(Type<T> value, Throwable th) {
        return new Try.Failure<T>(value, th);
    }

    public static <T> Try.Failure<T> failure(Throwable th) {
        return new Try.Failure<T>(th);
    }

    public static <A,B,F extends Function<A,B>> Function<A, Try<B>> wrap(final F fn) {
        return new Function<A, Try<B>>() {
            public Try<B> apply(A a) {
                return success(a).fmap(fn);
            }
        };
    }

    public static <A,B> Function<A,Try<B>> recover(final Function<A,B>... alternatives) {
        return new Function<A, Try<B>>() {
            public Try<B> apply(A a) {
                Try<B> result = null;
                for (Function<A,B> alternative : alternatives) {
                    result = success(a).fmap(alternative);
                    if (result.isSuccess()) {
                        return result;
                    }
                }
                return result;
            }
        };
    }

    public static <A,B> Try<B> Try(Function<A,B> fn) {
        return wrap(fn).apply(null);
    }

    public static <A,B> Try<B> Try(A a, Function<A,B> fn) {
        return wrap(fn).apply(a);
    }

}
