package fnz.data;

import java.util.List;

/**
 *
 */
public final class Fn {

    public static <A> Either.Left<A> Left(A source) {
        return Either.left(source);
    }

    public static <A> Either.Right<A> Right(A source) {
        return Either.right(source);
    }

    public static <A> Maybe.Just<A> Just(A source) {
        return Maybe.just(source);
    }

    public static <A> Maybe.Nothing<A> Nothing() {
        return Maybe.nothing();
    }

    public static <A> ListMonad<A> List(A... values) {
        return ListMonad.list(values);
    }

    public static <A> ListMonad<A> List(List<A> values) {
        return ListMonad.list(values);
    }

    public static <A,B,F extends Function<A,B>> Try Try(F fn) {
        try {
            return new Try.Success<A>(new Type(fn.apply(null)));
        } catch (Throwable th) {
            return new Try.Failure<A>(th);
        }
    }

    public static <A,B,F extends Function<A,B>> Try TryOrElse(F fn, F alternative) {
        try {
            return new Try.Success<A>(new Type(fn.apply(null)));
        } catch (Throwable anything) {
            try {
                return new Try.Success<A>(new Type(alternative.apply(null)));
            } catch (Throwable th) {
                return new Try.Failure<A>(th);
            }
        }

    }

    public static <A> Try<A> recover(Try<A> possibleFailure, Try<A> alternative) {
        return possibleFailure.recover(alternative);
    }

    public static <A,B,MA extends Monad<A>,MB extends Monad<B>> MB bind(MA ma, Function<A,MB> fn) {
       return ma.bind(fn);
    }

    public static <A,B, AA extends Applicative<A>, AB extends Applicative<B>> AB fapply(AA fa, Applicative<Function<A,B>> fn) {
        return (AB) fa.fapply(fn);
    }

    // tag::fmap[]
    public static <A,B, FA extends Functor<A>, FB extends Functor<B>> FB fmap(FA fa, Function<A,B> fn) {
        return fa.fmap(fn);
    }
    // end::fmap[]

    public static <A> A val(Monad<A> monad) {
        return monad != null ? monad.getTypedRef().getValue() : null;
    }

    public static <A> Maybe<A> maybe(Monad<A> monad) {
        if (monad == null) return Nothing();

        A value = monad.getTypedRef().getValue();

        return (Maybe<A>) (value != null ? Just(value) : Nothing()) ;
    }

}
