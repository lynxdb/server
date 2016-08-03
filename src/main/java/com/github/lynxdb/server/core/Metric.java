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
package com.github.lynxdb.server.core;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cambierr
 */
public class Metric extends Entry{

    private final String name;
    private final Map<String, String> tags;

    public Metric(String _name, Map<String, String> _tags, int _time, double _value) {
        super(_time, _value);
        name = _name;
        tags = _tags;
    }

    public Metric(String _name, int _time, double _value, Tag... _tags) {
        super(_time, _value);
        name = _name;
        Map<String, String> tagMap = new HashMap<>();
        for (Tag tag : _tags) {
            tagMap.put(tag.getKey(), tag.getValue());
        }
        tags = tagMap;
    }

    public Metric(com.github.lynxdb.server.api.http.mappers.Metric _m) {
        super((_m.timestamp > Integer.MAX_VALUE) ? (int) Math.floorDiv(_m.timestamp, 1000) : (int) _m.timestamp, _m.value);
        name = _m.metric;
        tags = _m.tags;

    }

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public static class Tag {

        private final String key;
        private final String value;

        public Tag(String _key, String _value) {
            key = _key;
            value = _value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

}
