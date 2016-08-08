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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lynxdb.server.api.http.mappers.UserCreationRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 *
 * @author honorem
 */
@Table("users")
@Component
public class User implements UserDetails {

    private static final ShaPasswordEncoder SHA_PASSWORD_ENCODER = new ShaPasswordEncoder(256);

    @PrimaryKeyColumn(name = "userLogin", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userLogin;
    private String userPassword;
    private UUID vhostId;
    private Rank rank;

    private User() {

    }

    protected User(UUID _vhostId, String _userLogin, String _userPassword, Rank _rank) {
        this.vhostId = _vhostId;
        this.userLogin = _userLogin;
        this.userPassword = _userPassword;
        this.rank = _rank;
    }

    public User(UserCreationRequest _ucr) {
        this.vhostId = _ucr.vhost;
        this.userLogin = _ucr.login;
        this.userPassword = SHA_PASSWORD_ENCODER.encodePassword(_ucr.password, getPasswordSalt());
        this.rank = _ucr.rank;
    }

    public boolean checkPassword(String _password) {
        return SHA_PASSWORD_ENCODER.isPasswordValid(userPassword, _password, getPasswordSalt());
    }

    private String getPasswordSalt() {
        return userLogin + vhostId;
    }

    public User setPassword(String _password) {
        userPassword = SHA_PASSWORD_ENCODER.encodePassword(_password, getPasswordSalt());
        return this;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return userPassword;
    }

    public String getLogin() {
        return userLogin;
    }

    public User setLogin(String _login) {
        userLogin = _login;
        return this;
    }

    public UUID getVhost() {
        return vhostId;
    }

    public Rank getRank() {
        return rank;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.vhostId.equals(Vhost.getSystemVhost().getId())) {
            return Arrays.asList(new SimpleGrantedAuthority(Rank.ADMIN.toRole()));
        }

        return Arrays.asList(new SimpleGrantedAuthority(rank.toRole()));
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return this.userLogin;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static enum Rank {
        RO_USER,
        RW_USER,
        ADMIN;

        public boolean canEdit() {
            return this.equals(RW_USER);
        }

        public String toRole() {
            return "ROLE_" + name();
        }

    }

    @Override
    public boolean equals(Object _other) {
        if (_other == null) {
            return false;
        }
        if (!(_other instanceof User)) {
            return false;
        }
        return ((User) _other).getLogin().equals(getLogin());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.userLogin);
        return hash;
    }

}
