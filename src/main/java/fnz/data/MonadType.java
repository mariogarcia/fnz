package fnz.data;

public abstract class MonadType<A> implements Monad<A>, Truth {

    private static final String EMPTY = "";

    private final Type<A> typedRef;

    protected MonadType(Type<A> valueRef) {
        this.typedRef = valueRef;
    }

    @Override
    public Type<A> getTypedRef() {
        return this.typedRef;
    }

    @Override
    public String toString() {
        return toStringSimple(this);
    }

    private String toStringSimple(Monad<A> monad) {
        String simpleName = monad.getClass().getSimpleName();
        A monadValue = monad.getTypedRef().getValue();
        String valueToStr = monadValue != null ? monadValue.toString() : EMPTY;

        return simpleName + "(" + valueToStr + ")";
    }

    @Override
    public <U> U get() {
        return (U) this.getTypedRef().getValue();
    }

}
