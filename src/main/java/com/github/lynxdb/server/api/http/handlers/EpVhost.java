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
import com.github.lynxdb.server.api.http.mappers.VhostCreationRequest;
import com.github.lynxdb.server.core.Vhost;
import com.github.lynxdb.server.core.repository.VhostRepo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
@RequestMapping(EpVhost.ENDPOINT)
public class EpVhost {

    public static final String ENDPOINT = "/api/vhost";
    
    @Autowired
    private VhostRepo vhosts;
    
    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createVhost(Authentication _authentication,
            @RequestBody @Valid VhostCreationRequest _vcr, BindingResult _bindingResult) {

        if (_bindingResult.hasErrors()) {
            ArrayList<String> errors = new ArrayList();
            _bindingResult.getFieldErrors().forEach((FieldError t) -> {
                errors.add(t.getField() + ": " + t.getDefaultMessage());
            });
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, errors.toString()).response();
        }
        
        Vhost v = new Vhost(_vcr);

        vhosts.save(v);
        
        return ResponseEntity.ok(v);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVhosts(Authentication _authentication) {

        List<Vhost> vhostList = vhosts.all();

        return ResponseEntity.ok(vhostList);
    }

    @RequestMapping(value = "/{vhostUUID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteVhost(
            Authentication _authentication,
            @PathVariable("vhostUUID") UUID vhostId) {

        Vhost vhost = vhosts.byId(vhostId);

        if(vhost == null){
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, "Vhost does not exist.", null).response();
        }
        
        vhosts.delete(vhost);

        return ResponseEntity.noContent().build();
    }
}
