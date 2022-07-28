package com.mastfrog.asmgraph.asm.sigs;

import com.mastfrog.asmgraph.asm.sigs.SomeFieldAnno.InnerAnno;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author timb
 */
public class Fields<C extends CharSequence> {

    @SomeFieldAnno(enumValue = SomeEnum.THAT, intValue = 42, longValue = 5, inner = @InnerAnno("what?"))
    public static final List<String> PUBLIC_STATIC_FINAL_LIST_OF_STRINGS = Arrays.asList("Woohoo");
    protected static List<String> PROTECTED_STATIC_LIST_OF_STRINGS = new ArrayList<>();

    public long longField = 1;
    public int inField = 2;
    public short shortField =3 ;
    public byte byteField=4;
    public char charField=5;
    public boolean boolField=true;

    public long[] longArrayField=new long[] {6,7};
    public int[] inArrayField=new int[] {8, 9};
    public short[] shortArrayField = new short[] {10, 11};
    public byte[] byteArrayField = new byte[]{12, 13};
    public char[] charArrayField = new char[] {'a', 'b'};
    public boolean[] boolArrayField = new boolean[] {false, true};

    public C typeParameterField;

    public Iterable<? extends C> iterTypeParameterExtendsField;
    public Iterable<? super C> iterTypeParameterSuperField;

}
