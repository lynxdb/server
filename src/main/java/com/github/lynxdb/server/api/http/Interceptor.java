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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 *
 * @author honorem
 */
@Component
public class Interceptor extends HandlerInterceptorAdapter {

    @Autowired
    HttpMonitor monitor;

    @Override
    public boolean preHandle(HttpServletRequest _request, HttpServletResponse _response, Object _handler) {
        _request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest _request, HttpServletResponse _response, Object _handler, Exception _ex) {

        long time = System.currentTimeMillis() - (long) _request.getAttribute("startTime");

        int status = _response.getStatus();

        if (status < 400) {
            if (_request.getRequestURI().startsWith("/api/query")) {
                monitor.queryOK.incrementAndGet();
                monitor.queryLatencies.add((int) time);
            } else if (_request.getRequestURI().startsWith("/api/put")) {
                monitor.putOK.incrementAndGet();
                monitor.putLatencies.add((int) time);
            }else{
                monitor.httpLatencies.add((int) time);
            }
            monitor.http2xx.incrementAndGet();
        } else {
            if (_request.getRequestURI().startsWith("/api/query")) {
                monitor.queryFAIL.incrementAndGet();
            } else if (_request.getRequestURI().startsWith("/api/put")) {
                monitor.putFAIL.incrementAndGet();
            }
            if (status < 500) {
                monitor.http4xx.incrementAndGet();
            } else {
                monitor.http5xx.incrementAndGet();
            }
        }
    }

}
