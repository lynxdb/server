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
package com.github.lynxdb.server.core;

import com.github.lynxdb.server.common.ChainableIterator;
import com.github.lynxdb.server.common.SuperIterator;
import com.github.lynxdb.server.query.Query;

/**
 *
 * @author cambierr
 */
public class Rate {

    public static TimeSerie compute(TimeSerie _source, Query.RateOptions _options) {
        SuperIterator<Entry> sie = new SuperIterator<>(_source);
        sie.next();

        return new TimeSerie(_source.getName(), _source.getTags(), new ChainableIterator<Entry>() {

            boolean lastZero = false;

            @Override
            public boolean hasNext() {
                return sie.hasNext();
            }

            @Override
            public Entry next() {
                sie.next();
                double dif = sie.getPrevious().getValue() - sie.getCurrent().getValue();
                if (_options.isCounter() && dif < 0) {
                    if (_options.getCounterMax() != Long.MAX_VALUE) {
                        dif += _options.getCounterMax();
                    }
                    // if value > resetValue -> return 0 and warn the next value about that
                    if (_options.getResetValue() != 0 && sie.getPrevious().getValue() > _options.getResetValue()) {
                        lastZero = true;
                        return new Entry(sie.getPrevious().getTime(), 0);
                    } else {
                        lastZero = false;
                        return new Entry(sie.getPrevious().getTime(), sie.getPrevious().getValue() - ((lastZero) ? 0 : sie.getCurrent().getValue()));
                    }
                } else {
                    // that's a rate so Dv/Dt
                    return new Entry(sie.getPrevious().getTime(), dif / (sie.getPrevious().getTime() - sie.getCurrent().getTime()));
                }
            }
        });
    }

}
