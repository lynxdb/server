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
