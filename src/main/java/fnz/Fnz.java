package fnz;

import fnz.data.Try;
import fnz.data.Monad;
import fnz.data.Either;
import fnz.data.Maybe;
import fnz.data.Functor;
import fnz.data.Function;
import fnz.data.TypeAwareFunction;
import fnz.data.ListMonad;
import fnz.data.Applicative;

/**
 *
 */
public final class Fnz {

    public static <A> Either.Left<A> Left(A source) {
        return Either.left(source);
    }

    public static <A> Either.Right<A> Right(A source) {
        return Either.right(source);
    }

    public static <A> Either<A> Either(A source) {
        return (Either<A>) (source != null ? Right(source) : Left(source));
    }

    public static <A> Maybe.Just<A> Just(A source) {
        return Maybe.just(source);
    }

    public static <A> Maybe.Nothing<A> Nothing() {
        return Maybe.nothing();
    }

    public static <A> Maybe<A> Maybe(A source) {
        return Maybe.maybe(source);
    }

    public static <A,M extends Monad<A>> Maybe<A> Maybe(M source) {
        return Maybe.maybe(source);
    }

    public static <A> Try.Success<A> Success(A source) {
        return Try.success(source);
    }

    public static <A> Try.Failure<A> Failure() {
        return Try.failure(null, new NullPointerException());
    }

    public static <A> ListMonad<A> List(A... values) {
        return ListMonad.list(values);
    }

    public static <A> ListMonad<A> List(Iterable<A> values) {
        return ListMonad.list(values);
    }

    public static <A,B> Try<B> Try(Function<A,B> fn) {
        return Try.Try(fn);
    }

    public static <A,B> Try<B> Try(A a, Function<A,B> fn) {
        return Try.Try(a, fn);
    }

    public static <A,B,F extends Function<A,B>> Function<A, Try<B>> wrap(F fn) {
        return Try.wrap(fn);
    }

    public static <A,B> Function<A,Try<B>> recover(Function<A,B>... alternatives) {
        return Try.recover(alternatives);
    }

    public static <A,B,MA extends Monad<A>,MB extends Monad<B>> MB bind(MA ma, Function<A,MB> fn) {
       return ma.bind(fn);
    }

    public static <A,B,MA extends Monad<A>,MB extends Monad<B>> MB bind2(MA ma, TypeAwareFunction<A,MB> fn) {
        return ma.bind2(fn);
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

}
