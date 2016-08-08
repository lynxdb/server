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
