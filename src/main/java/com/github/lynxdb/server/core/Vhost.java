/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.core;

import com.github.lynxdb.server.api.http.mappers.VhostCreationRequest;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/**
 *
 * @author honorem
 */
@Table("vhosts")
public class Vhost {
    
    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    
    @PrimaryKeyColumn(name = "vhostId", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID vhostId;
    private String vhostName;

    private Vhost() {

    }

    protected Vhost(UUID _vhostId, String _vhostName) {
        this.vhostId = _vhostId;
        this.vhostName = _vhostName;
    }

    public Vhost(String _vhostName) {
        this(UUID.randomUUID(), _vhostName);
    }

    public Vhost(VhostCreationRequest _vcr) {
        this(_vcr.name);
    }
    
    public UUID getId() {
        return vhostId;
    }
    
    public String getName() {
        return vhostName;
    }

    public Vhost setName(String _name) {
        vhostName = _name;
        return this;
    }
    
    @Override
    public boolean equals(Object _other) {
        if (_other == null) {
            return false;
        }
        if (!(_other instanceof Vhost)) {
            return false;
        }
        return ((Vhost) _other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.vhostId);
        return hash;
    }
    
    
    public static Vhost getSystemVhost(){
        return new Vhost(SYSTEM_UUID, "system");
    }
}
