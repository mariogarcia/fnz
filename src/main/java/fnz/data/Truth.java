package fnz.data;

/**
 * All classes implementing this inteface will be capable of
 * participate of the Groovy truth.
 *
 */
public interface Truth {

    /**
     * This method has to be implemented to know whether the
     * object implementing this interface should be treated
     * as a Boolean.TRUE value or Boolean.FALSE
     *
     * This interface has been created in order to
     * make it easier to interact with Groovy collections
     * and participate of the Groovy truth
     *
     * @return
     */
    public Boolean asBoolean();

}
