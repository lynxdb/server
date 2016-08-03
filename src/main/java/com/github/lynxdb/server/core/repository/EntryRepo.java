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

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.common.TagsSerializer;
import com.github.lynxdb.server.core.Metric;
import com.github.lynxdb.server.core.Vhost;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author honorem
 */
@Repository
public class EntryRepo {

    public static final int GROUP_WIDTH = 86400*7;
    
    @Autowired
    private CassandraTemplate ct;
    
    @Autowired
    private CassandraMonitor monitor;

    public void insert(Vhost _vhost, Metric _metric) {
        ArrayList<Insert> inserts = new ArrayList<>();
        processMetric(_vhost, inserts, _metric);
        ct.execute(QueryBuilder.batch(inserts.toArray(new Insert[inserts.size()])));
        
        monitor.queryBatchCount.incrementAndGet();
        monitor.queryPutCount.addAndGet(inserts.size());
    }

    public void insertBulk(Vhost _vhost, List<Metric> _metricList) {
        ArrayList<Insert> inserts = new ArrayList<>();
        _metricList.stream().forEach((m) -> {
            processMetric(_vhost, inserts, m);
        });
        ct.execute(QueryBuilder.batch(inserts.toArray(new Insert[inserts.size()])));
        
        monitor.queryBatchCount.incrementAndGet();
        monitor.queryPutCount.addAndGet(inserts.size());
    }

    public static int getGroup(long _timestamp) {
        return (int) Math.floorDiv(_timestamp, GROUP_WIDTH);
    }

    public void processMetric(Vhost _vhost, List<Insert> _list, Metric _metric) {
        int group = getGroup(_metric.getTime());
        String tags = TagsSerializer.serialize(_metric.getTags());

        _list.add(QueryBuilder.insertInto("series")
                .value("vhostid", _vhost.getId())
                .value("name", _metric.getName())
                .value("tags", tags)
                .value("group", group)
                .value("time", _metric.getTime())
                .value("random", (short) (Short.MAX_VALUE * Math.random()))
                .value("value", _metric.getValue()));
        _list.add(QueryBuilder.insertInto("list")
                .value("vhostid", _vhost.getId())
                .value("name", _metric.getName())
                .value("group", group)
                .value("tags", tags));
        _list.add(QueryBuilder.insertInto("suggest_name")
                .value("vhostid", _vhost.getId())
                .value("name", _metric.getName()));
        for (Map.Entry t : _metric.getTags().entrySet()) {
            _list.add(QueryBuilder.insertInto("suggest_tagk")
                    .value("vhostid", _vhost.getId())
                    .value("tagk", t.getKey()));
            _list.add(QueryBuilder.insertInto("suggest_tagv")
                    .value("vhostid", _vhost.getId())
                    .value("tagv", t.getValue()));
        }
    }

}
