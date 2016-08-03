/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.api.http.mappers;

import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author honorem
 */
public class VhostCreationRequest {
    @NotEmpty
    public String name;
}
