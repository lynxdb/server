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

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.core.Vhost;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author honorem
 */
@Repository
public class SuggestRepo {

    @Autowired
    private CassandraTemplate ct;
    
    @Autowired
    private CassandraMonitor monitor;

    public List<String> byTagValue(Vhost _vhost) {
        return byTagValue(_vhost, "");
    }

    public List<String> byTagValue(Vhost _vhost, String _query) {
        List<String> result = ct.select(QueryBuilder.select("tagv").from("suggest_tagv").where(QueryBuilder.eq("vhostid", _vhost.getId())).limit(Integer.MAX_VALUE), String.class);
        if (_query.isEmpty()) {
            return result;
        }
        result.removeIf((s -> !s.contains(_query)));
        
        monitor.queryGetCount.incrementAndGet();
        
        return result;
    }

    public List<String> byTagKey(Vhost _vhost) {
        return byTagKey(_vhost, "");
    }

    public List<String> byTagKey(Vhost _vhost, String _query) {
        List<String> result = ct.select(QueryBuilder.select("tagk").from("suggest_tagk").where(QueryBuilder.eq("vhostid", _vhost.getId())).limit(Integer.MAX_VALUE), String.class);
        if (_query.isEmpty()) {
            return result;
        }
        result.removeIf((s -> !s.contains(_query)));
        
        monitor.queryGetCount.incrementAndGet();
        
        return result;
    }

    public List<String> byName(Vhost _vhost) {
        return byName(_vhost, "");
    }

    public List<String> byName(Vhost _vhost, String _query) {
        List<String> result = ct.select(QueryBuilder.select("name").from("suggest_name").where(QueryBuilder.eq("vhostid", _vhost.getId())).limit(Integer.MAX_VALUE), String.class);
        if (_query.isEmpty()) {
            return result;
        }
        result.removeIf((s -> !s.contains(_query)));
        
        monitor.queryGetCount.incrementAndGet();
        
        return result;
    }
}
