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
package com.github.lynxdb.server.common;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;

/**
 *
 * @author honorem
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration{

    @Override
    protected String getKeyspaceName() {
        return System.getenv("cassandra_keyspace");
    }
    
    @Override
    protected String getContactPoints() {
        return System.getenv("cassandra_contactpoints");
    }
    
    @Override
    protected AuthProvider getAuthProvider() {
        return new PlainTextAuthProvider(System.getenv("cassandra_username"), System.getenv("cassandra_password"));
    }

    @Override
    protected ReconnectionPolicy getReconnectionPolicy() {
        return new ConstantReconnectionPolicy(500);
    }

    @Override
    protected RetryPolicy getRetryPolicy() {
        return new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE);
    }

    @Override
    protected LoadBalancingPolicy getLoadBalancingPolicy() {
        DCAwareRoundRobinPolicy.Builder builder = DCAwareRoundRobinPolicy.builder();
        if (System.getenv("cassandra_dc") != null) {
            builder.withLocalDc(System.getenv("cassandra_dc"));
        }
        return builder.build();
    }
    
    
    
}
