/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
