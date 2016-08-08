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
