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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lynxdb.server.api.http.ErrorResponse;
import com.github.lynxdb.server.api.http.mappers.QueryRequest;
import com.github.lynxdb.server.core.Aggregator;
import com.github.lynxdb.server.core.Aggregators;
import com.github.lynxdb.server.core.Entry;
import com.github.lynxdb.server.core.TimeSerie;
import com.github.lynxdb.server.core.User;
import com.github.lynxdb.server.core.Vhost;
import com.github.lynxdb.server.core.repository.VhostRepo;
import com.github.lynxdb.server.exception.InvalidTimeException;
import com.github.lynxdb.server.exception.ParsingQueryException;
import com.github.lynxdb.server.query.Engine;
import com.github.lynxdb.server.query.Query;
import com.github.lynxdb.server.query.Query.Builder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cambierr
 */
@RestController
@RequestMapping(EpQuery.ENDPOINT)
public class EpQuery {

    public static final String ENDPOINT = "/api/query";

    @Autowired
    private Aggregators aggregators;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Engine engine;

    @Autowired
    private VhostRepo vhosts;

    @RequestMapping(path = "", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE}, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rootJson(
            @RequestBody @Valid QueryRequest _request,
            Authentication _authentication,
            HttpServletResponse _response) {

        User user = (User) _authentication.getPrincipal();

        List<Query> queries;

        try {
            queries = parseQuery(vhosts.byId(user.getVhost()), _request);
        } catch (ParsingQueryException ex) {
            return new ErrorResponse(mapper, HttpStatus.BAD_REQUEST, ex.getMessage(), ex).response();
        }

        File f;
        FileOutputStream fos;
        try {
            f = File.createTempFile("lynx.", ".tmp");
            fos = new FileOutputStream(f);
        } catch (IOException ex) {
            return new ErrorResponse(mapper, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex).response();
        }

        try {
            saveResponse(fos, queries);
        } catch (IOException ex) {
            return new ErrorResponse(mapper, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex).response();
        }

        try {
            return ResponseEntity.ok(new InputStreamResource(new FileInputStream(f)));
        } catch (FileNotFoundException ex) {
            return new ErrorResponse(mapper, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex).response();
        } finally {
            f.delete();
        }
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity rootForm(
            @RequestBody String _request,
            Authentication _authentication,
            HttpServletResponse _response) {

        QueryRequest request;
        try {
            String decoded = URLDecoder.decode(_request, "UTF-8");
            decoded = decoded.substring(0, decoded.length() - 1);
            request = mapper.readValue(decoded, QueryRequest.class);
        } catch (IOException ex) {
            return new ErrorResponse(mapper, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex).response();
        }

        return rootJson(request, _authentication, _response);

    }

    @RequestMapping(path = "/exp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exp() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/gexp", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity gexp() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @RequestMapping(path = "/last", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity last(QueryRequest _request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    private List<Query> parseQuery(Vhost _vhost, QueryRequest _request) throws ParsingQueryException {
        List<Query> queries = new ArrayList<>();
        for (QueryRequest.Query q : _request.queries) {
            Builder b = Query.newBuilder();

            b.setAction(Query.Action.GET);
            b.setName(q.metric);
            if (q.tags != null) {
                b.setTags(q.tags);
            }
            b.setVhost(_vhost);
            b.setRate(q.rate);
            if (q.rateOptions != null) {
                Query.RateOptions.Builder rob = Query.RateOptions.newBuilder();

                rob.setCounter(q.rateOptions.counter);
                rob.setCounterMax(q.rateOptions.counterMax);
                rob.setResetValue(q.rateOptions.resetValue);

                b.setRateOptions(rob.build());
            }

            Aggregator agg = aggregators.get().get(q.aggregator);

            if (agg == null) {
                throw new ParsingQueryException("Aggregator " + q.aggregator + " does not exist.");
            }

            b.setAggregator(agg);

            try {
                b.setStart(QueryRequest.parseTime(_request.start));
            } catch (InvalidTimeException ex) {
                throw new ParsingQueryException(ex.getMessage());
            }

            if (_request.end == null) {
                b.setEnd(Math.floorDiv(System.currentTimeMillis(), 1000));
            } else {
                try {
                    b.setEnd(QueryRequest.parseTime(_request.end));
                } catch (InvalidTimeException ex) {
                    throw new ParsingQueryException(ex.getMessage());
                }
            }

            if (q.downsample != null && !q.downsample.isEmpty()) {
                Query.Downsampling.Builder dsb = Query.Downsampling.newBuilder();

                try {
                    long time = QueryRequest.parseDuration(q.downsample.split("-")[0]);
                    if (time >= 1000) {
                        dsb.setPeriod((int) Math.floorDiv(time, 1000));
                    } else {
                        throw new ParsingQueryException("Downsampling period too short. Must be greater than or equal to 1s");
                    }
                } catch (InvalidTimeException ex) {
                    throw new ParsingQueryException(ex.getMessage());
                }
                Aggregator dsagg = aggregators.get().get(q.downsample.split("-")[1]);

                if (dsagg == null) {
                    throw new ParsingQueryException("Aggregator " + q.downsample.split("-")[1] + " does not exist.");
                }
                dsb.setAggregator(dsagg);

                b.setDownsampling(dsb.build());
            }

            queries.add(b.build());

        }

        return queries;
    }

    private void saveResponse(OutputStream _output, List<Query> _queries) throws IOException {
        JsonFactory jFactory = new JsonFactory();
        JsonGenerator jGenerator;

        jGenerator = jFactory.createGenerator(_output, JsonEncoding.UTF8);
        jGenerator.writeStartArray();

        for (Query q : _queries) {
            TimeSerie ts;

            ts = engine.query(q);

            jGenerator.writeStartObject();

            jGenerator.writeStringField("metric", q.getName());

            //tags
            jGenerator.writeObjectFieldStart("tags");
            if (q.getTags() != null) {
                for (String tagk : q.getTags().keySet()) {
                    jGenerator.writeStringField(tagk, q.getTags().get(tagk));
                }
            }
            jGenerator.writeEndObject();

            //dps
            jGenerator.writeObjectFieldStart("dps");
            while (ts.hasNext()) {
                Entry e = ts.next();
                jGenerator.writeNumberField(String.valueOf(e.getTime()), e.getValue());
            }
            jGenerator.writeEndObject();

            //endQuery
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndArray();
        jGenerator.close();
        _output.flush();
        _output.close();

    }

}
