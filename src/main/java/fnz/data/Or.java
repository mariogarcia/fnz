package fnz.data;

/**
 * All types implementing this interface should implement the method
 * or(). This method allows to have a positive alternative to the current
 * monadic value.
 *
 */
public interface Or<T,M extends Monad<T>> {

    /**
     * If the semantics of the current monadic value indicate
     * that is not valid then the method should return the value
     * passed as parameter, otherwise it should return itself
     *
     * @param monad possible alternative to the current value
     * @return current value if valud otherwise it will return
     * the value passed as parameter
     */
    public M or(M monad);

    /**
     * If the semantics of the current monadic value indicate
     * that is not valid then the method should return the value
     * computed by the function  passed as parameter ,
     * otherwise it should return itself
     *
     * @param monad possible alternative computation to the current value
     * @return current value if valid otherwise it will return
     * the value passed as parameter
     */
    public M or(Function<T,M> monadGenerator);

}
