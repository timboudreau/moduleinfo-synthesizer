package notremapped;

import com.mastfrog.reloclib.RStrings;
import com.mastfrog.reloclib.RThing;
import com.mastfrog.reloclib.SomeAnno;

/**
 *
 * @author timb
 */
@SomeAnno("I'm not remapped, but my annotation is")
public class NotRemapped {
    private final String[] arr;
    
    public NotRemapped(String... arr) {
        this.arr = arr;
    }
    
    public RThing[] things() {
        return RStrings.things(arr);
    }
}
