/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.api.http.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lynxdb.server.api.http.ErrorResponse;
import com.github.lynxdb.server.api.http.mappers.UserCreationRequest;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.repository.UserRepo;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author honorem
 */
@RestController
@RequestMapping(EpUser.ENDPOINT)
public class EpUser {

    public static final String ENDPOINT = "/api/user";

    @Autowired
    private UserRepo users;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(
            Authentication _authentication,
            @RequestBody @Valid UserCreationRequest _ucr, BindingResult _bindingResult) {

        if (_bindingResult.hasErrors()) {
            ArrayList<String> errors = new ArrayList();
            _bindingResult.getFieldErrors().forEach((FieldError t) -> {
                errors.add(t.getField() + ": " + t.getDefaultMessage());
            });
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, errors.toString()).response();
        }
        
        User u = new User(_ucr);

        if (users.create(u)) {
            return ResponseEntity.ok(u);
        } else {
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, "User already exists").response();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUsers(Authentication _authentication) {

        List<User> userList = users.all();

        return ResponseEntity.ok(userList);
    }

    @RequestMapping(value = "/{userLogin}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateUser(
            Authentication _authentication,
            @PathVariable("userLogin") String userLogin,
            @RequestBody @Valid UserCreationRequest _ucr, BindingResult _bindingResult) {

        if (_bindingResult.hasErrors()) {
            ArrayList<String> errors = new ArrayList();
            _bindingResult.getFieldErrors().forEach((FieldError t) -> {
                errors.add(t.getField() + ": " + t.getDefaultMessage());
            });
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, errors.toString()).response();
        }
        
        User user = users.byLogin(userLogin);
        if (user == null) {
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, "User does not exist.", null).response();
        }

        user.setPassword(userLogin);
        
        users.save(user);

        return ResponseEntity.ok(user);
    }
    
    @RequestMapping(value = "/{userLogin}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteUser(
            Authentication _authentication,
            @PathVariable("userLogin") String userLogin) {

        User user = users.byLogin(userLogin);
        if (user == null) {
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, "User does not exist.", null).response();
        }

        users.delete(user);

        return ResponseEntity.noContent().build();
    }
}
