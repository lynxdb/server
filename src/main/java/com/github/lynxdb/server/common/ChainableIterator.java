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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An inproved version of {@link Iterator Iterator} that offers chaining
 * mecanisms
 *
 * @author cambierr
 * @param <T> the type of object returned
 * @see Iterator
 */
public abstract class ChainableIterator<T extends Object> implements Iterator<T> {

    /**
     * Apply a global transformation that returns a complete new
     * {@link ChainableIterator ChainableIterator}
     *
     * @param <R> The type of object returned by the new
     * {@link ChainableIterator ChainableIterator}
     * @param _f The transformation function to be applied to this
     * {@link ChainableIterator ChainableIterator}
     * @return a new transformed {@link ChainableIterator ChainableIterator}
     */
    public <R extends Object> ChainableIterator<R> lift(Func2<T, R> _f) {
        return _f.apply(this);
    }

    /**
     * Apply a per-entry transformation that only changes the returned value
     *
     * @param <R> The type of the returned valued
     * @param _f The transformation function
     * @return a new transformed {@link ChainableIterator ChainableIterator}
     */
    public <R extends Object> ChainableIterator<R> map(Func1<T, R> _f) {

        return new ChainableIterator<R>() {
            @Override
            public boolean hasNext() {
                return ChainableIterator.this.hasNext();
            }

            @Override
            public R next() {
                return _f.apply(ChainableIterator.this.next());
            }
        };
    }

    /**
     * Group all items of this {@link ChainableIterator ChainableIterator} into
     * a list
     *
     * @return The list of emitted objects
     */
    public List<T> toList() {
        List<T> list = new ArrayList();

        while (hasNext()) {
            list.add(next());
        }

        return list;
    }

    /**
     * Reduce all items of this {@link ChainableIterator ChainableIterator} to a
     * single value
     *
     * @param <R> The type of the return value
     * @param _f The reduce function
     * @return The reduced value
     */
    public <R extends Object> R reduce(Func3<T, R> _f) {
        return _f.apply(this);
    }

    /**
     * Create a {@link ChainableIterator ChainableIterator} from a standard java {@link Iterator Iterator}
     * @param <T> the type of object returned
     * @param _source The source {@link Iterator Iterator} to wrap
     * @return a {@link ChainableIterator ChainableIterator} mirroring the provided {@link Iterator Iterator}
     */
    public static <T extends Object> ChainableIterator<T> from(Iterator<T> _source) {
        if (_source instanceof ChainableIterator) {
            return (ChainableIterator<T>) _source;
        }
        return new ChainableIterator<T>() {
            @Override
            public boolean hasNext() {
                return _source.hasNext();
            }

            @Override
            public T next() {
                return _source.next();
            }
        };
    }

    /**
     * One-to-One transformation function
     * @param <R> The input object type
     * @param <T> The output object type
     */
    public static interface Func1<R extends Object, T extends Object> {

        public T apply(R _source);
    }

    /**
     * Many-to-Many transformation function
     * @param <R> The input object type
     * @param <T> The output object type
     */
    public static interface Func2<R extends Object, T extends Object> {

        public ChainableIterator<T> apply(ChainableIterator<R> _source);

    }

    /**
     * Many-to-One transformation function
     * @param <R> The input object type
     * @param <T> The output object type
     */
    public static interface Func3<R extends Object, T extends Object> {

        public T apply(ChainableIterator<R> _source);

    }
}
