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
package com.github.lynxdb.server.core.repository;

import com.github.lynxdb.server.common.Average;
import com.github.lynxdb.server.core.Metric;
import com.github.lynxdb.server.monitoring.Monitor;
import com.github.lynxdb.server.monitoring.Probe;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 *
 * @author cambierr
 */
@Component
public class CassandraMonitor implements Probe {

    public final Average tsScanQueryTime = new Average();
    public final Average tsFetchQueryTime = new Average();

    public final AtomicLong queryScanCount = new AtomicLong();
    public final AtomicLong queryFetchCount = new AtomicLong();
    public final AtomicLong queryDeleteCount = new AtomicLong();
    public final AtomicLong queryGetCount = new AtomicLong();
    public final AtomicLong queryPutCount = new AtomicLong();
    public final AtomicLong queryBatchCount = new AtomicLong();

    @Override
    public List<Metric> get() {
        int now = (int) (System.currentTimeMillis() / 1000);

        return Arrays.asList(
                new Metric(Monitor.PREFIX + ".cassandra.query.time", now, tsScanQueryTime.get(), new Metric.Tag("type", "scan")),
                new Metric(Monitor.PREFIX + ".cassandra.query.time", now, tsFetchQueryTime.get(), new Metric.Tag("type", "fetch")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst", now, queryScanCount.getAndSet(0), new Metric.Tag("type", "scan")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst", now, queryFetchCount.getAndSet(0), new Metric.Tag("type", "fetch")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst", now, queryDeleteCount.getAndSet(0), new Metric.Tag("type", "delete")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst", now, queryGetCount.getAndSet(0), new Metric.Tag("type", "get")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst", now, queryPutCount.getAndSet(0), new Metric.Tag("type", "put")),
                new Metric(Monitor.PREFIX + ".cassandra.rqst.batch", now, queryBatchCount.getAndSet(0))
        );
    }

}
