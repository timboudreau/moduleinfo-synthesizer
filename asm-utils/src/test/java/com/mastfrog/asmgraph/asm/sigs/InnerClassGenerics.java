/*
 * The MIT License
 *
 * Copyright 2022 Mastfrog Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.asmgraph.asm.sigs;

/**
 *
 * @author timb
 */
public class InnerClassGenerics<K, V> {

    static <K, V> void entry(InnerClassGenerics<K, V>.Entry en) {

    }

    static <K, V, T> void entryV(InnerClassGenerics<K, V>.Entry.Value<T> en) {

    }

    static <K, V, T> void entryVSub(InnerClassGenerics<K, V>.Entry.Value<T> en) {

    }

    static <K, V> void entrySub(InnerClassGenerics<K, V>.Entry.Sub en) {

    }

    static <K, V, R extends V> void entrySubValue(InnerClassGenerics<K, V>.Entry.Sub.SubValue<R> en) {

    }

    static <K, V, R extends V> void entrySubValueSub(InnerClassGenerics<K, V>.Entry.Sub.SubValue<R>.SubSubValue en) {

    }

    static <K, V, R extends V> void entrySubValueSub(InnerClassGenerics<K, V>.Entry.Sub.SubValue<R>.SubSubValue.SubSubSubValue en) {

    }

    static <K, V, R extends V, M> void entrySubValueSubTyped(InnerClassGenerics<K, V>.Entry.Sub.SubValue<R>.SubSubValue.TypedSubSubSubValue<M> en) {

    }

    static <M> void entrySubValueWildcard(InnerClassGenerics<?, ?>.Entry.Sub.SubValue<?>.SubSubValue.TypedSubSubSubValue<M> en) {

    }
    
    static void foo(InnerClassGenerics.Entry e) {
        
    }

    class Entry {

        class Sub {

            class SubValue<R> {

                class SubSubValue {

                    class SubSubSubValue {

                    }

                    class TypedSubSubSubValue<M> {

                    }

                }
            }
        }

        class Value<T> {

            class VSub {

            }
        }
    }
}
