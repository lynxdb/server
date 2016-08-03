/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lynxdb.server.api.http.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author honorem
 */
@RestController
@RequestMapping(EpError.ENDPOINT)
public class EpError implements ErrorController {

    public static final String ENDPOINT = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity error(HttpServletRequest request, HttpServletResponse response) {
        
        HttpStatus status = HttpStatus.valueOf((int) request.getAttribute("javax.servlet.error.status_code"));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();
        error.put("code", status.value());
        error.put("message", status.getReasonPhrase());
        error.put("details", getErrorAttributes(request, true).get("message").toString());
        if(getErrorAttributes(request, true).get("exception") != null){
            error.put("trace", getErrorAttributes(request, true).get("exception").toString()+"\n"+getErrorAttributes(request, true).get("trace").toString());
        }

        return ResponseEntity.status(status).body(error.toString());
    }

    @Override
    public String getErrorPath() {
        return ENDPOINT;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

}
