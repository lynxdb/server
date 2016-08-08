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
package com.github.lynxdb.server.api.http.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lynxdb.server.api.http.ErrorResponse;
import com.github.lynxdb.server.api.http.mappers.UserCreationRequest;
import com.github.lynxdb.server.api.http.mappers.UserUpdateRequest;
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
            @RequestBody @Valid UserUpdateRequest _ucr, BindingResult _bindingResult) {

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

        if (_ucr.password != null && !_ucr.password.isEmpty()) {
            user.setPassword(_ucr.password);
        }
        if(_ucr.rank != null){
            user.setRank(_ucr.rank);
        }

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
