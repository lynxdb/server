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
package com.github.lynxdb.server.common;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 *
 * @author honorem
 */
public class TagsSerializer {

    public static String serialize(Map<String, String> _tags) {
        if (_tags.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner("\\;");
        _tags.keySet().stream().forEach((s) -> {
            sj.add(s + "\\=" + _tags.get(s));
        });
        return sj.toString();
    }

    public static Map<String, String> deserialize(String _tags) {
        HashMap<String, String> tags = new HashMap();
        if (_tags == null || _tags.isEmpty()) {
            return tags;
        }
        for (String entry : _tags.split("\\\\;")) {
            String[] keyVal = entry.split("\\\\=");
            tags.put(keyVal[0], keyVal[1]);
        }
        return tags;
    }
}
