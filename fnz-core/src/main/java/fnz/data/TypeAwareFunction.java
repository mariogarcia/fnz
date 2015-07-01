package fnz.data;

/**
 *
 */
public interface TypeAwareFunction<A,B> {
    B apply(Class<B> resultClazz, A input);
}
