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
import com.github.lynxdb.server.api.http.mappers.Metric;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.repository.EntryRepo;
import com.github.lynxdb.server.core.repository.VhostRepo;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cambierr
 */
@RestController
@RequestMapping(EpPut.ENDPOINT)
public class EpPut {

    public static final String ENDPOINT = "/api/put";
    
    @Autowired
    private EntryRepo entries;

    @Autowired
    private VhostRepo vhosts;
    
    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(path = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity put(Authentication _authentication,
            @RequestBody @Valid List<Metric> _request, BindingResult _bindingResult) {
        
        User user = (User) _authentication.getPrincipal();
        
        if (_bindingResult.hasErrors()) {
            ArrayList<String> errors = new ArrayList();
            _bindingResult.getFieldErrors().forEach((FieldError t) -> {
                errors.add(t.getField() + ": " + t.getDefaultMessage());
            });
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, errors.toString(), null).response();
        }

        List<com.github.lynxdb.server.core.Metric> metricList = new ArrayList<>();
        _request.stream().forEach((m) -> {
            metricList.add(new com.github.lynxdb.server.core.Metric(m));
        });

        try {
            entries.insertBulk(vhosts.byId(user.getVhost()), metricList);
        } catch (Exception ex) {
            throw ex;
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
