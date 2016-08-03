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
package com.github.lynxdb.server.monitoring;

import com.github.lynxdb.server.common.HostnameProvider;
import com.github.lynxdb.server.core.Metric;
import com.github.lynxdb.server.core.Vhost;
import com.github.lynxdb.server.core.repository.EntryRepo;
import com.github.lynxdb.server.run.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author cambierr
 */
@Service
public class Monitor extends TimerTask implements Bundle{

    private static final long MONITORING_TIME = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);
    public static final String PREFIX = "lynx";
    public static final int AVG_SIZE = 100;

    private final Timer scheduler = new Timer(true);
    private final List<Probe> probes = new ArrayList<>();
    @Autowired
    private EntryRepo entries;
    @Autowired
    private ApplicationContext appC;
    @Autowired
    private HostnameProvider hostname;

    @Override
    public void start() throws Exception {
        appC.getBeansOfType(Probe.class).forEach((String t, Probe u) -> {
            LOGGER.debug("Found probe " + u.getClass().getName());
            probes.add(u);
        });

        scheduler.scheduleAtFixedRate(this, 0, MONITORING_TIME);
    }

    @Override
    public void stop() throws Exception {
        scheduler.cancel();
        scheduler.purge();
        probes.clear();
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void run() {
        LOGGER.debug("Starting run");
        List<Metric> metrics = new ArrayList<>();

        probes.forEach((Probe p) -> {
            LOGGER.debug("Querying probe " + p.getClass().getName());
            p.get().forEach((Metric t) -> {
                LOGGER.debug("Probe " + p.getClass().getName() + " returned metric " + t.getName() + t.getTags());
                metrics.add(t);
                t.getTags().put("host", hostname.get());
            });
        });
        
        LOGGER.debug("Saving "+metrics.size()+" metrics");
        entries.insertBulk(Vhost.getSystemVhost(), metrics);

        LOGGER.debug("Run complete");
    }

    @Override
    public boolean autostart() {
        return true;
    }

}
