package com.mastfrog.asmgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Just a thread-safe list of names with a reverse index lookup and a lock.
 *
 * @author Tim Boudreau
 */
public final class IntIndex<T extends Comparable<T>> implements Iterable<T> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<T> names = new ArrayList<>();
    private final Map<T, Integer> index = new HashMap<>();

    public int indexOf(T what) {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            return index.getOrDefault(what, -1);
        } finally {
            readLock.unlock();
        }
    }

    public synchronized int addOrGet(T what) {
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            Integer ix = index.get(what);
            if (ix == null) {
                ix = names.size();
                names.add(what);
                index.put(what, ix);
            }
            return ix;
        } finally {
            writeLock.unlock();
        }
    }
    
    public List<T> snapshot() {
        List<T> copy;
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            copy = new ArrayList<>(names);
        } finally {
            readLock.unlock();
        }
        return copy;
        
    }

    @Override
    public Iterator<T> iterator() {
        return snapshot().iterator();
    }
}
