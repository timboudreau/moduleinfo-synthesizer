package com.mastfrog.asmgraph.asm.sigs;

import com.mastfrog.function.state.Dbl;
import java.util.function.DoubleConsumer;

/**
 *
 * @author timb
 */
public class FakeStats {
    private double[] values;
    
    public void forEach(DoubleConsumer c) {
        for (int i = 0; i < values.length; i++) {
            c.accept(values[i]);
        }
    }
    
    public double average1() {
        Dbl dbl = Dbl.create();
        forEach(d -> dbl.add(d));
        return dbl.getAsDouble() / values.length;
    }
    
    public double average2() {
        Dbl dbl = Dbl.create();
        forEach(dbl::add);
        return dbl.getAsDouble() / values.length;
    }
    
    double size() {
        return values.length;
    }
}
