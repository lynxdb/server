/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 *
 * @author honorem
 */
@Table("users")
public class User implements UserDetails {

    public static final int BCRYPT_ROUNDS = 12;

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
        this(_ucr.vhost, _ucr.login, BCrypt.hashpw(_ucr.password, BCrypt.gensalt(BCRYPT_ROUNDS)), _ucr.rank);
    }

    public boolean checkPassword(String _password) {
        return BCrypt.checkpw(_password, userPassword);
    }

    public User setPassword(String _password) {
        userPassword = BCrypt.hashpw(_password, BCrypt.gensalt(BCRYPT_ROUNDS));
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
