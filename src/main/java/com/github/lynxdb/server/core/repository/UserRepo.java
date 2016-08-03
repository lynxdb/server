/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.core.repository;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.core.User;
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
public class UserRepo {

    @Autowired
    private CassandraTemplate ct;

    @Autowired
    private CassandraMonitor monitor;

    public User byLogin(String _login) {
        monitor.queryGetCount.incrementAndGet();
        return ct.selectOne(QueryBuilder.select().all().from("users").where(QueryBuilder.eq("userlogin", _login)).limit(1), User.class);
    }

    public List<User> all() {
        monitor.queryGetCount.incrementAndGet();
        return ct.select(QueryBuilder.select().all().from("users").limit(Integer.MAX_VALUE), User.class);
    }

    public List<User> byVhost(Vhost _vhost) {
        monitor.queryGetCount.incrementAndGet();
        return ct.select(QueryBuilder.select().all().from("users_by_vhost").where(QueryBuilder.eq("vhostid", _vhost.getId())).limit(Integer.MAX_VALUE), User.class);
    }

    public boolean create(User _user) {
        monitor.queryPutCount.incrementAndGet();
        ResultSetFuture rsf = ct.executeAsynchronously(CassandraTemplate.createInsertQuery("users", _user, null, ct.getConverter()).ifNotExists());
        Row r = rsf.getUninterruptibly().one();
        return r.getBool("[applied]");
    }

    public User save(User _user) {
        monitor.queryPutCount.incrementAndGet();
        ct.update(_user);
        return _user;
    }

    public void delete(User _user) {
        monitor.queryDeleteCount.incrementAndGet();
        ct.delete(_user);
    }
}
