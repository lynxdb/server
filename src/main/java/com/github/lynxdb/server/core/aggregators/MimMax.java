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
package com.github.lynxdb.server.core.aggregators;

import com.github.lynxdb.server.core.Aggregator;
import com.github.lynxdb.server.core.Entry;
import com.github.lynxdb.server.core.TimeSerie;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author cambierr
 */
@Component
public class MimMax extends Aggregator {

    public final static String NAME = "mimmax";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public TimeSerie aggregate(List<TimeSerie> _series) {
        return doDefaultIfMissing(_series, new Aggregator.Reducer() {
            double max;

            @Override
            public void update(Entry _entry) {
                if (_entry.getValue() > max) {
                    max = _entry.getValue();
                }
            }

            @Override
            public double result() {
                return max;
            }

            @Override
            public void reset() {
                max = Double.MIN_VALUE;
            }
        }, Double.MIN_VALUE);
    }

    @Override
    public TimeSerie downsample(TimeSerie _serie, long _period) {
        return doDownsampling(_serie, _period, new Aggregator.Reducer() {
            double max;

            @Override
            public void update(Entry _entry) {
                if (_entry.getValue() > max) {
                    max = _entry.getValue();
                }
            }

            @Override
            public double result() {
                return max;
            }

            @Override
            public void reset() {
                max = Double.MIN_VALUE;
            }
        });
    }

}
