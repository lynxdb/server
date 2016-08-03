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
package com.github.lynxdb.server.query;

import com.github.lynxdb.server.core.Aggregator;
import com.github.lynxdb.server.core.Vhost;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author cambierr
 */
public class Query {

    private UUID vhost;
    private Action action;
    private long start;
    private long end;
    private Aggregator aggregator;
    private String name;
    private boolean rate;
    private RateOptions rateOptions;
    private Downsampling downsampling;
    private Map<String, String> tags;

    private Query() {
        action = Action.GET;
        start = Long.MIN_VALUE;
        end = System.currentTimeMillis() / 1000;
        aggregator = null;
        name = null;
        rate = false;
        rateOptions = null;
        downsampling = null;
        tags = null;
        vhost = null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Action getAction() {
        return action;
    }

    public UUID getVhost() {
        return vhost;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }

    public String getName() {
        return name;
    }

    public boolean isRate() {
        return rate;
    }

    public RateOptions getRateOptions() {
        return rateOptions;
    }

    public Downsampling getDownsampling() {
        return downsampling;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public static class Builder {

        private Query instance;

        private Builder() {
            instance = new Query();
        }

        public Builder setAction(Action _action) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.action = _action;
            return this;
        }

        public Builder setVhost(Vhost _vhost) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.vhost = _vhost.getId();
            return this;
        }

        public Builder setVhost(UUID _vhost) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.vhost = _vhost;
            return this;
        }

        public Builder setStart(long _start) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.start = _start;
            return this;
        }

        public Builder setEnd(long _end) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.end = _end;
            return this;
        }

        public Builder setAggregator(Aggregator _aggregator) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.aggregator = _aggregator;
            return this;
        }

        public Builder setName(String _name) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.name = _name;
            return this;
        }

        public Builder setRate(boolean _rate) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.rate = _rate;
            if (!_rate) {
                instance.rateOptions = null;
            } else{
                instance.rateOptions = new RateOptions();
            }
            return this;
        }

        public Builder setRateOptions(RateOptions _rateOptions) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.rateOptions = _rateOptions;
            instance.rate = true;
            return this;
        }

        public Builder setDownsampling(Downsampling _downsampling) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.downsampling = _downsampling;
            return this;
        }

        public Builder setTags(Map<String, String> _tags) {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            instance.tags = Collections.unmodifiableMap(_tags);
            return this;
        }

        public Query build() {
            if (instance == null) {
                throw new IllegalStateException("Builder already used");
            }
            Query q = instance;
            instance = null;
            if (q.getAction() == null) {
                throw new IllegalArgumentException("action not provided");
            }
            if (q.getVhost() == null) {
                throw new IllegalArgumentException("vhost not provided");
            }
            if (q.getStart() > q.getEnd()) {
                throw new IllegalArgumentException("start time must be lower than end time");
            }
            if (q.getAggregator() == null) {
                throw new IllegalArgumentException("aggregator not provided");
            }
            if (q.getName() == null) {
                throw new IllegalArgumentException("name not provided");
            }
            return q;
        }

    }

    public static class Downsampling {

        private int period;
        private Aggregator aggregator;

        private Downsampling() {
            period = Integer.MIN_VALUE;
            aggregator = null;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public long getPeriod() {
            return period;
        }

        public Aggregator getAggregator() {
            return aggregator;
        }

        public static class Builder {

            private Downsampling instance;

            private Builder() {
                instance = new Downsampling();
            }

            public Builder setPeriod(int _period) {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                if(_period < 1){
                    throw new IllegalArgumentException("downsampling interval must be at least one second");
                }
                instance.period = _period;
                return this;
            }

            public Builder setPeriod(long _count, TimeUnit _unit) {
                return setPeriod((int) _unit.toSeconds(_count));
            }

            public Builder setAggregator(Aggregator _aggregator) {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                instance.aggregator = _aggregator;
                return this;
            }

            public Downsampling build() {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                Downsampling ds = instance;
                instance = null;
                if (ds.getAggregator() == null) {
                    throw new IllegalArgumentException("aggregator not provided");
                }
                if (ds.getPeriod() == Long.MIN_VALUE) {
                    throw new IllegalArgumentException("aggregator not provided");
                }
                return ds;
            }

        }

        public static enum FillPolicy {
            NONE,
            NAN,
            NULL,
            ZERO
        }
    }

    public static class RateOptions {

        private boolean counter;
        private long counterMax;
        private long resetValue;

        private RateOptions() {
            counter = false;
            counterMax = Long.MAX_VALUE;
            resetValue = Long.MAX_VALUE;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public boolean isCounter() {
            return counter;
        }

        public long getCounterMax() {
            return counterMax;
        }

        public long getResetValue() {
            return resetValue;
        }

        public static class Builder {

            private RateOptions instance;

            private Builder() {
                instance = new RateOptions();
            }

            public Builder setCounter(boolean _counter) {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                instance.counter = _counter;
                return this;
            }

            public Builder setCounterMax(long _counterMax) {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                instance.counterMax = _counterMax;
                return this;
            }

            public Builder setResetValue(long _resetValue) {
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                instance.resetValue = _resetValue;
                return this;
            }

            public RateOptions build() {
                RateOptions ro = instance;
                instance = null;
                if (instance == null) {
                    throw new IllegalStateException("Builder already used");
                }
                return ro;
            }

        }

    }

    public static enum Action {
        GET,
        DELETE
    }
}
