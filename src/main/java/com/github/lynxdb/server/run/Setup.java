/*
 * The MIT License
 *
 * Copyright 2016 honorem.
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
package com.github.lynxdb.server.run;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.Vhost;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.data.cassandra.core.CassandraTemplate;

/**
 *
 * @author honorem
 */
public class Setup {

    private static CassandraTemplate ct;

    public static void main(String[] args) {

        init();

        Vhost system = Vhost.getSystemVhost();

        CreateKeyspaceSpecification keyspaceSpec = new CreateKeyspaceSpecification(System.getenv("cassandra_keyspace"))
                .ifNotExists()
                .withSimpleReplication(1);

        ct.execute(keyspaceSpec);

        init(System.getenv("cassandra_keyspace"));

        CreateTableSpecification vhosts = CreateTableSpecification.createTable("vhosts")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .column("vhostname", DataType.text());

        ct.execute(vhosts);

        CreateTableSpecification users = CreateTableSpecification.createTable("users")
                .ifNotExists()
                .partitionKeyColumn("userlogin", DataType.text())
                .column("userpassword", DataType.text())
                .column("rank", DataType.text())
                .column("vhostid", DataType.uuid());

        ct.execute(users);

        ct.getSession().execute("CREATE MATERIALIZED VIEW IF NOT EXISTS lynx.users_by_vhost AS SELECT * FROM lynx.users WHERE vhostid IS NOT NULL PRIMARY KEY (vhostid, userlogin)");

        CreateTableSpecification suggest_tagk = CreateTableSpecification.createTable("suggest_tagk")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .clusteredKeyColumn("tagk", DataType.text());

        ct.execute(suggest_tagk);

        CreateTableSpecification suggest_tagv = CreateTableSpecification.createTable("suggest_tagv")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .clusteredKeyColumn("tagv", DataType.text());

        ct.execute(suggest_tagv);

        CreateTableSpecification suggest_name = CreateTableSpecification.createTable("suggest_name")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .clusteredKeyColumn("name", DataType.text());

        ct.execute(suggest_name);

        CreateTableSpecification list = CreateTableSpecification.createTable("list")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .partitionKeyColumn("name", DataType.text())
                .clusteredKeyColumn("group", DataType.cint())
                .clusteredKeyColumn("tags", DataType.text());

        ct.execute(list);

        CreateTableSpecification series = CreateTableSpecification.createTable("series")
                .ifNotExists()
                .partitionKeyColumn("vhostid", DataType.uuid())
                .partitionKeyColumn("name", DataType.text())
                .partitionKeyColumn("tags", DataType.text())
                .partitionKeyColumn("group", DataType.cint())
                .clusteredKeyColumn("time", DataType.cint())
                .clusteredKeyColumn("random", DataType.smallint())
                .column("value", DataType.cdouble());

        ct.execute(series);

        Insert systemVhost = QueryBuilder.insertInto("vhosts")
                .value("vhostid", system.getId())
                .value("vhostname", system.getName());

        ct.execute(systemVhost);

        Insert adminUser = QueryBuilder.insertInto("users")
                .value("userlogin", "admin")
                .value("userpassword", "2fa2849275b7af3e4137bcd867423462a409c1c264f7c5d8f67c3985429a6557")
                .value("vhostid", system.getId())
                .value("rank", User.Rank.RW_USER.name());

        ct.execute(adminUser);

        ct.getSession().getCluster().close();

        System.exit(0);

    }

    private static void init() {

        Cluster cluster = Cluster.builder()
                .addContactPoint(System.getenv("cassandra_contactpoints"))
                .build();

        ct = new CassandraTemplate(cluster.connect());
    }

    private static void init(String _keyspace) {

        Cluster cluster = Cluster.builder()
                .addContactPoint(System.getenv("cassandra_contactpoints"))
                .build();

        ct = new CassandraTemplate(cluster.connect(_keyspace));
    }

}
