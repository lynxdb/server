/*
 * The MIT License
 *
 * Copyright 2016 cambierr.
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
package com.github.lynxdb.server.common;

import com.github.lynxdb.server.exception.WtfException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author cambierr
 * @param <T>
 */
public class EvictingQueue<T extends Object> implements Iterable<T> {

    private final int size;
    private final ConcurrentLinkedQueue<T> bakingList;

    public EvictingQueue(int _size) {
        size = _size;
        bakingList = new ConcurrentLinkedQueue<>();
    }

    public void add(T _entry) {
        while (bakingList.size() >= size) {
            bakingList.poll();
        }
        bakingList.add(_entry);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            Iterator<T> source = bakingList.iterator();

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public T next() {
                return source.next();
            }

            @Override
            public void remove() {
                throw new WtfException("You should not remove values yourself!");
            }
        };
    }

}
