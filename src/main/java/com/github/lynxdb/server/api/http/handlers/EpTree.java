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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cambierr
 */
@RestController
@RequestMapping(EpTree.ENDPOINT)
public class EpTree {

    public static final String ENDPOINT = "/api/tree";
    
    @RequestMapping(path = "/", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity root() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/branch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity branch() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/collisions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity collisions() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/notmatched", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity notmatched() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/rule", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rule() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/rules", method = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rules() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity test() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}
