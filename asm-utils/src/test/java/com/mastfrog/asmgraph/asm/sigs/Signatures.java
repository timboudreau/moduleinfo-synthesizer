package com.mastfrog.asmgraph.asm.sigs;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author timb
 */
public class Signatures<S extends Number & CharSequence & Iterable<String>>
        extends Thing<String, Arglet, IllegalThreadStateException>
        implements IfaceOne, IfaceTwo<Short> {

    public void noArgsNoReturn() {

    }
    
    public void noArgsThrows() throws SomeException, IOException {
        
    }

    @Override
    public boolean retBool() {
        return true;
    }

    @Override
    public short retShort() {
        return 2;
    }

    @Override
    public Short objShort() {
        return Short.valueOf("23");
    }
    
    protected SomeEnum takesAndReceivesEnum(SomeEnum arg) {
        return arg;
    }

    public byte retByte() {
        return 3;
    }

    public int retInt() {
        return 1;
    }

    public float retFloat() {
        return 4F;
    }

    public double retDouble() {
        return 5D;
    }

    public long retLong() {
        return 6L;
    }

    public char retChar() {
        return 'x';
    }

    public boolean[] retBoolArr() {
        return new boolean[0];
    }

    public short[] retShortArr() {
        return new short[0];
    }

    public byte[] retByteArr() {
        return new byte[0];
    }

    public int[] retIntArr() {
        return new int[0];
    }

    public float[] retFloatArr() {
        return new float[0];
    }

    public double[] retDoubleArr() {
        return new double[0];
    }

    public long[] retLongArr() {
        return new long[0];
    }

    public char[] retCharArr() {
        return new char[0];
    }

    public String[] returnStringArray() {
        return new String[0];
    }

    public CharSequence[] returnCharSequenceArray() {
        return new CharSequence[0];
    }

    public <T> T[] returnsGenericArray(Class<T> type) {
        return (T[]) Array.newInstance(type, 1);
    }

    public String returnsString() {
        return "Hello";
    }

    public CharSequence returnsCharSequence() {
        return "Goodbye";
    }

    public String[] takeStringArray(String[] arg) {
        return arg;
    }

    public Object[] takeArrays(byte[] bytes, short[] shorts, int[] ints, long[] longs, float[] floats, double[] doubles, boolean[] bools, char[] chars, Object[] objs) {
        return new Object[0];
    }

    public <K extends Key & CharSequence, V extends Number, M extends Map<? super K, ? extends V>> M stuff(List<? extends K> keys, List<? extends V> values, M into) {
        return null;
    }

    public <K extends Key & CharSequence, V extends Number, M extends Map<? super K, ? extends V>> M worse(
            List<? super K> keys, List<? extends V> values) {
        return null;
    }

    public <K extends Key, V extends S, M extends Map<? super K, ? extends V>> M simpler(
            List<? super K> keys, List<? extends V> values) {
        return null;
    }

    public <K, V extends S, M extends Map<? super K, ? extends V>> M noext(
            List<? super K> keys, List<? extends V> values) {
        return null;
    }

    public <K extends Object & Key, V extends S, M extends Map<? super K, ? extends V>> M pseudext(
            List<? super K> keys, List<? extends V> values) {
        return null;
    }

    public <K extends Object & Key, V extends S, M extends Triple<?, ? super K, ? extends V>> M triple(
            List<? super K> keys, List<? extends V> values) {
        return null;
    }

    public <M extends CharSequence> void foo(M sq) {

    }

    public <M extends Wook> void foo(M sq) {

    }

    public <K extends Iterable<S>, X extends K> Triple<X, CharSequence, K> reuse(Triple<? extends X, ? super K, ? super CharSequence> c) {
        return null;
    }

    public <K extends Iterable<S>, X> Triple<X, byte[], K> glorp(Triple<? extends X, ? super K, ? super CharSequence> c) {
        return null;
    }

    private <A extends B, B extends C, C extends String & CharSequence & Iterable<? extends CharSequence> & Consumer<? super B> & Comparable<String>>
            void snarled(Triple<A, ?, ? super C> triple) {

    }

    private void allWildcards(Triple<?, ?, ?> trip) {

    }

    public Map<String, Integer> stringInt() {
        return null;
    }

    public Map<byte[], short[][]> byteShort() {
        return null;
    }

    public void takesShort3(short[][][] s) {

    }

    public void takesString4(String[][][][] s) {

    }

    public boolean[][][] retBool3() {
        return null;
    }

    static abstract class Wook {

    }
}
