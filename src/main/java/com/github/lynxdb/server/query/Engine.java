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
package com.github.lynxdb.server.query;

import com.github.lynxdb.server.common.ChainableIterator;
import com.github.lynxdb.server.core.Entry;
import com.github.lynxdb.server.core.Rate;
import com.github.lynxdb.server.core.TimeSerie;
import com.github.lynxdb.server.core.repository.TimeSerieRepo;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The main Query engine. Returns a TS for each provided Query
 *
 * @author cambierr
 */
@Service
public class Engine {

    @Autowired
    private TimeSerieRepo tsr;

    /**
     * Default constructor. Just to make sure it always exists for spring.
     */
    public Engine() {

    }

    /**
     * Query the database using provided request
     *
     * @param _query The Query to be executed
     * @return A TimeSerie matching the request
     */
    public TimeSerie query(Query _query) {
        //scan the database for interesting rows, filter them, and return a list of raw TS
        ChainableIterator<TimeSerie> tss = tsr.find(_query.getVhost(), _query.getName(), _query.getTags(), _query.getStart(), _query.getEnd());

        if (!tss.hasNext()) {
            //If we don't have any TS from the database, return an empty one to avoid errors in aggregators
            return new TimeSerie(_query.getName(), new HashMap<>(), new ChainableIterator<Entry>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Entry next() {
                    return null;
                }
            });
        }

        // proceed to downsampling if required
        if (_query.getDownsampling() != null) {
            tss = tss.map((ChainableIterator.Func1<TimeSerie, TimeSerie>) (TimeSerie _source) -> _query.getDownsampling().getAggregator().downsample(_source, _query.getDownsampling().getPeriod()));
        }

        // proceed to aggregation
        TimeSerie ts = tss.reduce((ChainableIterator<TimeSerie> _source) -> _query.getAggregator().aggregate(_source.toList()));

        // if a rate is requested, process it here
        if (_query.isRate()) {
            ts = Rate.compute(ts, _query.getRateOptions());
        }

        return ts;
    }

}
