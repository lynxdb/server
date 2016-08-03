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
package com.github.lynxdb.server.api.http;

import com.github.lynxdb.server.api.http.handlers.EpAggregators;
import com.github.lynxdb.server.api.http.handlers.EpPut;
import com.github.lynxdb.server.api.http.handlers.EpQuery;
import com.github.lynxdb.server.api.http.handlers.EpSuggest;
import com.github.lynxdb.server.api.http.handlers.EpUser;
import com.github.lynxdb.server.api.http.handlers.EpVhost;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 *
 * @author honorem
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserRepo users;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        http.antMatcher("/api/**")
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(EpAggregators.ENDPOINT, EpQuery.ENDPOINT, EpSuggest.ENDPOINT).hasAnyRole(User.Rank.RO_USER.name(), User.Rank.RW_USER.name(), User.Rank.ADMIN.name())
                .antMatchers(HttpMethod.POST, EpPut.ENDPOINT).hasAnyRole(User.Rank.RW_USER.name(), User.Rank.ADMIN.name())
                .antMatchers(EpUser.ENDPOINT, EpVhost.ENDPOINT).hasRole(User.Rank.ADMIN.name());
        
        http.httpBasic().realmName("Lynx");
        
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Autowired
    public void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new AbstractUserDetailsAuthenticationProvider() {
            @Override
            protected void additionalAuthenticationChecks(UserDetails ud, UsernamePasswordAuthenticationToken upat) throws AuthenticationException {

            }

            @Override
            protected UserDetails retrieveUser(String string, UsernamePasswordAuthenticationToken upat) throws AuthenticationException {
                User user = users.byLogin(string);
                if (user == null) {
                    throw new UsernameNotFoundException("No such User : " + string);
                }
                if (user.checkPassword(upat.getCredentials().toString())) {
                    return user;
                } else {
                    throw new BadCredentialsException("Bad credentials");

                }
            }
        });
    }

}
