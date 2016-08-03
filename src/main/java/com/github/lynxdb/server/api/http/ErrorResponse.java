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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author honorem
 */
public class ErrorResponse {

    private final ObjectMapper mapper;

    private final HttpStatus status;
    private final String message;
    private final Throwable ex;

    public ErrorResponse(ObjectMapper _mapper, HttpStatus _status, String _message, Throwable _ex) {
        this.mapper = _mapper;
        this.status = _status;
        this.message = _message;
        this.ex = _ex;
    }
    
    public ErrorResponse(ObjectMapper _mapper, HttpStatus _status, String _message) {
        this(_mapper, _status, _message, null);
    }

    public ResponseEntity response() {
        ObjectNode error = mapper.createObjectNode();

        error.put("code", status.value());
        error.put("message", status.getReasonPhrase());
        if (message != null) {
            error.put("details", message);
        }
        if (ex != null) {
            StringBuilder trace = new StringBuilder();
            parseException(ex, trace);
            error.put("trace", trace.toString());
        }

        return ResponseEntity.status(status).body(error);
    }
    
    private void parseException(Throwable _thrw, StringBuilder _builder){
        _builder.append(_thrw.getClass().getName()).append(": ").append(_thrw.getMessage());
            for(StackTraceElement ste : _thrw.getStackTrace()){
                _builder.append("\t").append(ste.toString()).append("\n");
            }
            if(_thrw.getCause() != null){
                _builder.append("Caused by :");
                parseException(_thrw.getCause(), _builder);
            }
    }

}
