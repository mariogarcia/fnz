package fnz.control.lc

/**
 * This class is intended to be use in combination with another
 * Iterator instances.
 *
 * If the original iterator has produced the limit of results
 * expected by the TakeIterator instance or it doesn't have
 * more elements then the iteration will stop.
 *
 * There could be more than one original iterator associated
 * with one TakeIterator instance.
 */
class TakeIterator implements Iterator {

    Integer limit
    Integer counter = 0

    /**
     * This constructor takes as argument the maximum
     * number of valid results expected by this iterator
     *
     * @param limit
     */
    TakeIterator(Integer limit) {
        this.limit = limit
    }

    /**
     * This method associates a new Iterable object with the
     * current TakeIterator instance. This is done by creating a
     * new instance of a PreConditionIterator
     */
    public <A> Iterator<A> join(Iterable<A> iterable) {
        return new PreConditionIterator<A>(this, iterable.iterator())
    }

    /**
     * This method increases the valid results expected by the
     * take iterator.
     *
     * Once the internal counter reaches the limit then the
     * iterator will return false to the hasNext() method.
     */
    void advance() {
        this.counter += 1
    }

    @Override
    boolean hasNext() {
        return this.counter < limit
    }

    @Override
    def next() { /**/ }

    /**
     * Transforms the original iterator in a combination of the original
     * iterator and the current take iterator
     */
    static class PreConditionIterator<A> implements Iterator<A> {

        TakeIterator takeIterator
        Iterator<A> internal

        PreConditionIterator(TakeIterator takeIterator, Iterator<A> internal) {
            this.takeIterator = takeIterator
            this.internal = internal
        }

        boolean hasNext() {
            return [takeIterator, internal]*.hasNext().every()
        }

        A next() {
            return this.internal.next()
        }

    }

}
