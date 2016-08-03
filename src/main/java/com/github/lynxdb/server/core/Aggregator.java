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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 *
 * @author cambierr
 */
public abstract class Aggregator {

    public abstract TimeSerie aggregate(List<TimeSerie> _series);

    public abstract TimeSerie downsample(TimeSerie _serie, long _period);

    protected TimeSerie doDownsampling(TimeSerie _serie, long _period, Reducer _reducer) {
        return new TimeSerie(_serie.getName(), _serie.getTags(), new ChainableIterator<Entry>() {

            int lower;
            Entry last = null;

            @Override
            public boolean hasNext() {
                return _serie.hasNext() || last != null;
            }

            @Override
            public Entry next() {
                _reducer.reset();
                if (last == null) {
                    Entry e = _serie.next();
                    lower = (int) (_period * Math.floorDiv(e.getTime(), _period));
                    _reducer.update(e);
                } else {
                    lower = (int) (_period * Math.floorDiv(last.getTime(), _period));
                    _reducer.update(last);
                    last = null;
                }
                while (_serie.hasNext()) {
                    last = _serie.next();
                    if (last.getTime() > lower) {
                        _reducer.update(last);
                    } else {
                        break;
                    }
                }
                return new Entry(lower, _reducer.result());
            }
        });
    }

    protected TimeSerie doInterpolate(List<TimeSerie> _series, Reducer _reducer) {
        Assert.notEmpty(_series);

        List<SuperIterator> sil = new ArrayList<>();

        _series.forEach((TimeSerie t) -> {
            if (t.hasNext()) {
                SuperIterator<Entry> si = new SuperIterator<>(t);
                si.next();
                sil.add(si);
            }
        });

        Map<String, String> tags = new HashMap<>();
        tags.putAll(_series.get(0).getTags());

        _series.forEach((TimeSerie t) -> {
            Iterator<Map.Entry<String, String>> i = tags.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> e = i.next();
                if (!t.getTags().containsKey(e.getKey()) || !t.getTags().get(e.getKey()).equals(e.getValue())) {
                    i.remove();
                }

            }
        });

        return new TimeSerie(_series.get(0).getName(), tags, new ChainableIterator<Entry>() {

            @Override
            public boolean hasNext() {
                return sil.stream().anyMatch((SuperIterator t) -> t.hasNext() || t.getCurrent() != null);
            }

            @Override
            public Entry next() {
                _reducer.reset();

                Iterator<SuperIterator> rr = sil.iterator();
                while (rr.hasNext()) {
                    if (rr.next().getCurrent() == null) {
                        rr.remove();
                    }
                }

                int max = Integer.MIN_VALUE;

                for (SuperIterator<Entry> r : sil) {
                    max = Integer.max(max, r.getCurrent().getTime());
                }

                for (SuperIterator<Entry> r : sil) {
                    if (r.getCurrent().getTime() == max) {
                        _reducer.update(r.getCurrent());
                        r.next();
                    } else if (r.getPrevious() != null) {
                        _reducer.update(new Entry(max, interpolate(r.getCurrent(), r.getPrevious(), max)));
                    }
                }
                return new Entry(max, _reducer.result());
            }
        });
    }

    protected TimeSerie doDefaultIfMissing(List<TimeSerie> _series, Reducer _reducer, double _default) {
        Assert.notEmpty(_series);

        List<SuperIterator> sil = new ArrayList<>();

        _series.forEach((TimeSerie t) -> {
            if (t.hasNext()) {
                SuperIterator<Entry> si = new SuperIterator<>(t);
                si.next();
                sil.add(si);
            }
        });

        Map<String, String> tags = new HashMap<>();
        tags.putAll(_series.get(0).getTags());

        _series.forEach((TimeSerie t) -> {
            Iterator<Map.Entry<String, String>> i = tags.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> e = i.next();
                if (!t.getTags().containsKey(e.getKey()) || !t.getTags().get(e.getKey()).equals(e.getValue())) {
                    i.remove();
                }

            }
        });

        return new TimeSerie(_series.get(0).getName(), tags, new ChainableIterator<Entry>() {

            @Override
            public boolean hasNext() {
                return sil.stream().anyMatch((SuperIterator t) -> t.hasNext() || t.getCurrent() != null);
            }

            @Override
            public Entry next() {
                _reducer.reset();

                Iterator<SuperIterator> rr = sil.iterator();
                while (rr.hasNext()) {
                    if (rr.next().getCurrent() == null) {
                        rr.remove();
                    }
                }

                int max = Integer.MIN_VALUE;

                for (SuperIterator<Entry> r : sil) {
                    max = Integer.max(max, r.getCurrent().getTime());
                }

                for (SuperIterator<Entry> r : sil) {
                    if (r.getCurrent().getTime() == max) {
                        _reducer.update(r.getCurrent());
                        r.next();
                    } else if (r.getPrevious() != null) {
                        _reducer.update(new Entry(max, _default));
                    }
                }
                return new Entry(max, _reducer.result());
            }
        });
    }

    private double interpolate(Entry _left, Entry _right, long _time) {
        return _left.getTime() + (_time - _left.getTime()) * (_right.getValue() - _left.getValue()) / (_right.getTime() - _left.getTime());
    }

    protected TimeSerie doMerge(List<TimeSerie> _series, Reducer _reducer) {
        TimeSerie merged = TimeSerie.merge(_series);
        return new TimeSerie(merged.getName(), merged.getTags(), new ChainableIterator<Entry>() {

            int time;
            Entry last = null;

            @Override
            public boolean hasNext() {
                return merged.hasNext() || last != null;
            }

            @Override
            public Entry next() {
                _reducer.reset();
                if (last == null) {
                    Entry e = merged.next();
                    time = e.getTime();
                    _reducer.update(e);
                } else {
                    time = last.getTime();
                    _reducer.update(last);
                    last = null;
                }
                while (merged.hasNext()) {
                    last = merged.next();
                    if (last.getTime() == time) {
                        _reducer.update(last);
                    } else {
                        break;
                    }
                }
                return new Entry(time, _reducer.result());
            }
        });
    }

    public abstract String getName();

    public static abstract class Reducer {

        public abstract void update(Entry _entry);

        public abstract double result();

        public abstract void reset();

    }

}
