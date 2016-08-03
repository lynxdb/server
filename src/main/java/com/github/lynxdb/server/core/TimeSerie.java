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
public class TimeSerie extends ChainableIterator<Entry> {

    private final String name;
    private final Map<String, String> tags;
    private final ChainableIterator<Entry> source;

    public TimeSerie(String _name, Map<String, String> _tags, ChainableIterator<Entry> _source) {
        Assert.notNull(_name);
        Assert.notNull(_source);
        name = _name;
        tags = _tags;
        source = _source;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public Entry next() {
        return source.next();
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public static TimeSerie merge(List<TimeSerie> _series) {
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
                return sil.stream().anyMatch((superIterator) -> superIterator.hasNext() || superIterator.getCurrent() != null);
            }

            @Override
            public Entry next() {

                Iterator<SuperIterator> rr = sil.iterator();
                while (rr.hasNext()) {
                    if (rr.next().getCurrent() == null) {
                        rr.remove();
                    }
                }

                long max = Long.MIN_VALUE;
                for (SuperIterator<Entry> r : sil) {
                    max = Long.max(max, r.getCurrent().getTime());
                }
                for (SuperIterator<Entry> r : sil) {
                    if (r.getCurrent().getTime() == max) {
                        r.next();
                        return r.getPrevious();
                    }
                }

                throw new IllegalStateException("something went wrong");
            }
        });
    }

}
