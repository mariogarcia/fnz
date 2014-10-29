package fnz.data;

import java.util.List;

/**
 *
 */
public class TypeCollection<A> extends Type<List<A>> {

    public TypeCollection(List<A> col) {
        super(col);
    }

}
