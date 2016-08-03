/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.core.repository;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.Vhost;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author honorem
 */
@Repository
public class VhostRepo {

    @Autowired
    private CassandraTemplate ct;

    @Autowired
    private UserRepo users;

    @Autowired
    private CassandraMonitor monitor;

    public Vhost byId(UUID _id) {
        monitor.queryGetCount.incrementAndGet();
        return ct.selectOne(QueryBuilder.select().all().from("vhosts").where(QueryBuilder.eq("vhostid", _id)).limit(1), Vhost.class);
    }

    public List<Vhost> all() {
        monitor.queryGetCount.incrementAndGet();
        return ct.select(QueryBuilder.select().all().from("vhosts").limit(Integer.MAX_VALUE), Vhost.class);
    }

    public Vhost save(Vhost _vhost) {
        ct.insert(_vhost);
        monitor.queryPutCount.incrementAndGet();
        return _vhost;
    }

    public void delete(Vhost _vhost) {
        List<Delete> deletes = new ArrayList<>();
        users.byVhost(_vhost).forEach((User t) -> {
            deletes.add(CassandraTemplate.createDeleteQuery("users", t, null, ct.getConverter()));
        });
        deletes.add(CassandraTemplate.createDeleteQuery("vhosts", _vhost, null, ct.getConverter()));
        ct.execute(QueryBuilder.batch(deletes.toArray(new Delete[deletes.size()])));

        monitor.queryBatchCount.incrementAndGet();
        monitor.queryDeleteCount.addAndGet(deletes.size());
    }
}
