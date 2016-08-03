/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.api.http.mappers;

import com.github.lynxdb.server.core.User;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author honorem
 */
public class UserCreationRequest {

    public UUID vhost;
    @NotEmpty
    @Size(min = 3)
    public String login;
    @NotEmpty
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    public String password;
    @NotNull
    public User.Rank rank;
}
