package com.mastfrog.reloclib;

import java.util.Objects;

/**
 *
 * @author timb
 */
@SomeAnno("I am a thing")
public class RThing {

    private final String what;

    public RThing(String what) {
        this.what = what;
    }
    
    public String toString() {
        return what;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.what);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RThing other = (RThing) obj;
        return Objects.equals(this.what, other.what);
    }
    
    
}
