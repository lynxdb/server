/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
