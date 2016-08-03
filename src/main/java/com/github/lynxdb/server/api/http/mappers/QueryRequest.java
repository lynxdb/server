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
package com.github.lynxdb.server.api.http.mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.lynxdb.server.exception.InvalidTimeException;
import com.github.lynxdb.server.validation.Aggregator;
import com.github.lynxdb.server.validation.DSAggregator;
import com.github.lynxdb.server.validation.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author honorem
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryRequest {
    
    @NotNull
    @Time
    public Object start;
    @Time
    public Object end = System.currentTimeMillis();
    @NotEmpty
    public List<Query> queries;
    public boolean showQuery = false;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {
        
        @NotNull
        @Aggregator
        public String aggregator;
        @DSAggregator
        public String downsample;
        @NotEmpty
        public String metric;
        public boolean rate = false;
        public RateOptions rateOptions;
        public Map<String, String> tags;
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RateOptions {
            
            public boolean counter = false;
            public long counterMax = Long.MAX_VALUE;
            public long resetValue = 0;
        }
    }
    
    /**
     * 
     * Only supporting UTC timezone rightnow
     * 
     * @param _time (String|Integer|Long) the time to Object to parse
     * @return the time in s
     * @throws InvalidTimeException 
     */
    public static long parseTime(Object _time) throws InvalidTimeException {
        if (_time instanceof Integer || _time instanceof Long) {
            if ((long) _time > Integer.MAX_VALUE) {
                return Math.floorDiv((long) _time, 1000);
            }
            return (long) _time;
        } else if (_time instanceof String) {
            String time = _time.toString();
            if (time.endsWith("-ago")) {
                long amount = parseDuration(time.substring(0, time.length() - 4));
                if (amount >= 1000) {
                    return Math.floorDiv((System.currentTimeMillis() - amount), 1000);
                } else {
                    throw new InvalidTimeException("Invalid time : " + _time + ". Must be greater than or equal to 1s");
                }
            } else {
                SimpleDateFormat fmt;
                switch (time.length()) {
                    case 10:
                        fmt = new SimpleDateFormat("yyyy/MM/dd");
                        break;
                    case 16:
                        if (time.contains("-")) {
                            fmt = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
                        } else {
                            fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                        }
                        break;
                    case 19:
                        if (time.contains("-")) {
                            fmt = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        } else {
                            fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        }
                        break;
                    default:
                        throw new InvalidTimeException("Invalid time date : " + _time);
                }
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    return Math.floorDiv(fmt.parse(time).getTime(), 1000);
                } catch (ParseException ex) {
                    throw new InvalidTimeException("Invalid time date : " + _time);
                }
            }
        } else {
            throw new InvalidTimeException("Unsupported time type : " + _time);
        }
    }

    /**
     * parse string to time
     *
     * supported suffix : ms, s, m, h, d, w, n, y
     *
     * @param _duration String to parse
     * @return time in ms
     * @throws InvalidTimeException if suffix is not supported
     */
    public static long parseDuration(String _duration) throws InvalidTimeException {
        long amount;
        if (_duration.endsWith("ms")) {
            amount = Long.parseLong(_duration.substring(0, _duration.length() - 2));
        } else {
            amount = Long.parseLong(_duration.substring(0, _duration.length() - 1));
            if (_duration.endsWith("s")) {
                amount *= 1000;
            } else if (_duration.endsWith("m")) {
                amount *= 1000 * 60;
            } else if (_duration.endsWith("h")) {
                amount *= 1000 * 60 * 60;
            } else if (_duration.endsWith("d")) {
                amount *= 1000 * 60 * 60 * 24;
            } else if (_duration.endsWith("w")) {
                amount *= 1000 * 60 * 60 * 24 * 7;
            } else if (_duration.endsWith("n")) {
                amount *= 1000 * 60 * 60 * 24 * 30;
            } else if (_duration.endsWith("y")) {
                amount *= 1000 * 60 * 60 * 24 * 365;
            } else {
                throw new InvalidTimeException("Invalid time (suffix) : " + _duration);
            }
        }
        
        return amount;
    }
    
}
