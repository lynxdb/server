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
import com.github.lynxdb.server.api.http.mappers.SuggestRequest;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.repository.SuggestRepo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cambierr
 */
@RestController
@RequestMapping(EpSuggest.ENDPOINT)
public class EpSuggest {

    public static final String ENDPOINT = "/api/suggest";
    
    @Autowired
    private SuggestRepo suggests;
    
    @Autowired
    private VhostRepo vhosts;
    
    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity post(
            @RequestBody @Valid SuggestRequest _request,
            Authentication _authentication,
            BindingResult _bindingResult) {

        if (_bindingResult.hasErrors()) {
            ArrayList<String> errors = new ArrayList();
            _bindingResult.getFieldErrors().forEach((FieldError t) -> {
                errors.add(t.getField() + ": " + t.getDefaultMessage());
            });
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, errors.toString(), null).response();
        }

        return response(_request, _authentication);
    }

    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(
            Authentication _authentication,
            @RequestParam(value = "max", required = false, defaultValue = "25") int _max,
            @RequestParam(value = "q", required = false, defaultValue = "") String _q,
            @RequestParam(value = "type", required = true) String _type) {

        SuggestRequest request = new SuggestRequest();
        request.type = _type;
        request.q = _q;
        request.max = _max;

        return response(request, _authentication);
    }

    private ResponseEntity response(SuggestRequest _request, Authentication _authentication) {

        User user = (User) _authentication.getPrincipal();

        List<String> result;

        switch (_request.type) {
            case "metrics":
                if (_request.q == null || _request.q.isEmpty()) {
                    result = suggests.byName(vhosts.byId(user.getVhost()));
                } else {
                    result = suggests.byName(vhosts.byId(user.getVhost()), _request.q);
                }
                break;
            case "tagv":
                if (_request.q == null || _request.q.isEmpty()) {
                    result = suggests.byTagValue(vhosts.byId(user.getVhost()));
                } else {
                    result = suggests.byTagValue(vhosts.byId(user.getVhost()), _request.q);
                }
                break;
            case "tagk":
                if (_request.q == null || _request.q.isEmpty()) {
                    result = suggests.byTagKey(vhosts.byId(user.getVhost()));
                } else {
                    result = suggests.byTagKey(vhosts.byId(user.getVhost()), _request.q);
                }
                break;
            default:
                return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, "Insupported suggest type : "+_request.type, null).response();
        }

        if (result.size() < _request.max) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok(result.subList(0, _request.max));
        }
    }

}
