package fnz.data;

/**
 *
 */
public class Type<A>  {

    private final A value;

    public Type(A value) {
        this.value = value;
    }

    public A getValue() {
        return this.value;
    }

}
