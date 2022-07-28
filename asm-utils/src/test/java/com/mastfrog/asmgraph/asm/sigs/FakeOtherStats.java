package com.mastfrog.asmgraph.asm.sigs;

import com.mastfrog.function.state.Dbl;

/**
 *
 * @author timb
 */
public class FakeOtherStats {

    private FakeStats fakeStats;

    public double average1() {
        /*
 average1 InvD accept (Lcom/mastfrog/function/state/Dbl;)Ljava/util/function/DoubleConsumer;
 TYPE1 (D)V
 TYPE2 (D)V
 HANDLE NAME lambda$average1$0
 HANDLE DESC (Lcom/mastfrog/function/state/Dbl;D)V
 HANDL OWNER com/mastfrog/asmgraph/asm/sigs/FakeOtherStats
 HANDLE  TAG6
 IFACE false        
        */
        Dbl dbl = Dbl.create();
        fakeStats.forEach(d -> dbl.add(d));
        return dbl.getAsDouble() / fakeStats.size();
    }

    public double average2() {
        /*
average2 InvD accept (Lcom/mastfrog/function/state/Dbl;)Ljava/util/function/DoubleConsumer;
 TYPE1 (D)V // what we are being coerced into (what forEach accepts)
 TYPE2 (D)V // ????
 HANDLE NAME add // the handle - in this case, a method reference, being invoked
 HANDLE DESC (D)D // the type of the method being invoked
 HANDL OWNER com/mastfrog/function/state/Dbl
 HANDLE  TAG9
 IFACE true
        */
        Dbl dbl = Dbl.create();
        fakeStats.forEach(dbl::add);
        return dbl.getAsDouble() / fakeStats.size();
    }

}
