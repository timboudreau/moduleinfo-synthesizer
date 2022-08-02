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
package com.mastfrog.asmgraph.info;

import com.mastfrog.asmgraph.asm.model.TypeName;
import java.util.function.Function;

/**
 * Conversion functions for type names.
 *
 * @author Tim Boudreau
 */
public enum TypeNameStyle implements TypeNameTransformer {
    INTERNAL,
    SOURCE,
    SOURCE_RAW,
    SOURCE_TRUNCATED,
    SOURCE_SIMPLE,
    SOURCE_RAW_SIMPLE;

    @Override
    public boolean isComplete() {
        return this == SOURCE || this == INTERNAL;
    }

    @Override
    public boolean includesGenerics() {
        return this != SOURCE_RAW && this != SOURCE_RAW_SIMPLE;
    }

    @Override
    public boolean includesPackages() {
        return this != SOURCE_SIMPLE && this != SOURCE_RAW_SIMPLE;
    }

    @Override
    public boolean isMachineFormat() {
        return this == INTERNAL;
    }

    public boolean isRaw() {
        return this == SOURCE_RAW || this == SOURCE_RAW_SIMPLE;
    }

    @Override
    public String apply(TypeName t) {
        switch (this) {
            case INTERNAL:
                return t.internalName();
            case SOURCE:
                return t.sourceName();
            case SOURCE_TRUNCATED:
                return t.sourceNameTruncated();
            case SOURCE_SIMPLE:
                return t.simpleName();
            case SOURCE_RAW:
                return t.rawName().sourceName();
            case SOURCE_RAW_SIMPLE:
                return t.rawName().simpleName();
            default:
                throw new AssertionError(this);
        }
    }
}
