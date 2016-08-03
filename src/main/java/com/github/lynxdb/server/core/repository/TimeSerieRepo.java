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

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.common.ChainableIterator;
import com.github.lynxdb.server.common.TagsSerializer;
import com.github.lynxdb.server.core.Entry;
import com.github.lynxdb.server.core.TimeSerie;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cambierr
 */
@Repository
public class TimeSerieRepo {

    @Autowired
    private CassandraTemplate ct;

    @Autowired
    private CassandraMonitor csMonit;

    public ChainableIterator<TimeSerie> find(UUID _vhost, String _name, Map<String, String> _tags, long _start, long _end) {

        return findRows(_vhost, _name, _tags, _start, _end)
                .map((ChainableIterator.Func1<ChainableIterator<TS>, TimeSerie>) (ChainableIterator<TS> _source) -> {
                    Map<String, String> tags = new HashMap<>();
                    AtomicBoolean tagsLoaded = new AtomicBoolean(false);

                    return new TimeSerie(_name, tags, new ChainableIterator<Entry>() {

                        Iterator<Row> current = null;

                        @Override
                        public boolean hasNext() {
                            if (current != null && current.hasNext()) {
                                return true;
                            }
                            while (_source.hasNext()) {
                                TS newTS = _source.next();
                                if (!tagsLoaded.get()) {
                                    tagsLoaded.set(true);
                                    tags.putAll(newTS.tags);
                                }
                                long start = System.currentTimeMillis();
                                current = ct.query(QueryBuilder
                                        .select("time", "value")
                                        .from("series")
                                        .where(QueryBuilder.eq("vhostid", _vhost))
                                        .and(QueryBuilder.eq("name", _name))
                                        .and(QueryBuilder.eq("tags", newTS.serialized))
                                        .and(QueryBuilder.eq("group", newTS.group))
                                        .and(QueryBuilder.gte("time", _start))
                                        .and(QueryBuilder.lte("time", _end))
                                        .orderBy(QueryBuilder.desc("time"))).iterator();
                                csMonit.tsFetchQueryTime.add((int) (System.currentTimeMillis() - start));
                                csMonit.queryFetchCount.incrementAndGet();
                                if (current.hasNext()) {
                                    return true;
                                }
                            }
                            return false;
                        }

                        @Override
                        public Entry next() {
                            Row r = current.next();
                            return new Entry(r.getInt("time"), r.getDouble("value"));
                        }
                    });
                });
    }

    private ChainableIterator<ChainableIterator<TS>> findRows(UUID _vhost, String _name, Map<String, String> _tags, long _start, long _end) {
        /**
         * @todo: I would like to find a way to avoid intense memory consumption
         * of this code. ie: avoiding the map<key,list<ts>> which stores all
         * rows to be queried... need to think about that later
         */
        return new ChainableIterator<ChainableIterator<TS>>() {

            Iterator<Map.Entry<Integer, List<TS>>> source = null;

            public void start() {
                long start = System.currentTimeMillis();
                source = ct
                        .query(
                                QueryBuilder.select("group", "tags").from("list")
                                .where(QueryBuilder.eq("vhostid", _vhost))
                                .and(QueryBuilder.eq("name", _name))
                                .and(QueryBuilder.gte("group", EntryRepo.getGroup(_start)))
                                .and(QueryBuilder.lte("group", EntryRepo.getGroup(_end)))
                                .orderBy(QueryBuilder.desc("group"))
                        )
                        .all()
                        .stream()
                        .map((Row t) -> new TS(t.getInt("group"), TagsSerializer.deserialize(t.getString("tags")), t.getString("tags")))
                        .filter((TS t) -> _tags == null || _tags.entrySet().stream().allMatch((Map.Entry<String, String> tt) -> tt.getValue().equals(t.tags.get(tt.getKey()))))
                        .collect(Collectors.groupingBy((TS t) -> t.tags.hashCode())).entrySet().iterator();
                csMonit.queryScanCount.incrementAndGet();
                csMonit.tsScanQueryTime.add((int) (System.currentTimeMillis() - start));
            }

            @Override
            public boolean hasNext() {
                if (source == null) {
                    start();
                }
                return source.hasNext();
            }

            @Override
            public ChainableIterator<TS> next() {
                if (source == null) {
                    start();
                }
                return ChainableIterator.from(source.next().getValue().iterator());
            }
        };
    }

    private class TS {

        public UUID vhost;
        public String name;
        public int group;
        public Map<String, String> tags;
        public String serialized;

        public TS(int _group, Map<String, String> _tags, String _serialized) {
            group = _group;
            tags = _tags;
            serialized = _serialized;
        }
    }

}
