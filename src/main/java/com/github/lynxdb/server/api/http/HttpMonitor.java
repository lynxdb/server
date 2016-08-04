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
package com.github.lynxdb.server.api.http;

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
public class HttpMonitor implements Probe {

    public final AtomicLong http2xx = new AtomicLong();
    public final AtomicLong http4xx = new AtomicLong();
    public final AtomicLong http5xx = new AtomicLong();

    public final AtomicLong queryOK = new AtomicLong();
    public final AtomicLong queryFAIL = new AtomicLong();

    public final AtomicLong putOK = new AtomicLong();
    public final AtomicLong putFAIL = new AtomicLong();

    public final Average queryLatencies = new Average();
    public final Average putLatencies = new Average();
    public final Average httpLatencies = new Average();

    @Override
    public List<Metric> get() {
        int now = (int) (System.currentTimeMillis() / 1000);

        return Arrays.asList(
                new Metric(Monitor.PREFIX + ".http.2xx", now, http2xx.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.4xx", now, http4xx.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.5xx", now, http5xx.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.query.ok", now, queryOK.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.query.fail", now, queryFAIL.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.put.ok", now, putOK.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.put.fail", now, putFAIL.getAndSet(0)),
                new Metric(Monitor.PREFIX + ".http.query.latency", now, queryLatencies.get()),
                new Metric(Monitor.PREFIX + ".http.put.latency", now, putLatencies.get()),
                new Metric(Monitor.PREFIX + ".http.latency", now, httpLatencies.get())
        );
    }

}
