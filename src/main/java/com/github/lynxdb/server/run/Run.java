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
package com.github.lynxdb.server.run;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

/**
 *
 * @author cambierr
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.github.lynxdb.server")
public class Run implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Run.class);
    private final List<Bundle> bundles = new ArrayList<>();

    @Autowired
    ApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    public static void main(String[] args) {
        SpringApplication.run(Run.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        if (Arrays.stream(args).anyMatch((String t) -> t.equals("--debug"))) {
            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.toLevel("debug"));
        }

        if (Arrays.stream(args).anyMatch((String t) -> t.equals("--trace"))) {
            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.toLevel("trace"));
        }

        LOGGER.info("Discovering bundles");
        context.getBeansOfType(Bundle.class)
                .entrySet()
                .stream()
                .filter((Map.Entry<String, Bundle> b) -> b.getValue().autostart() || Arrays
                        .stream(args)
                        .anyMatch((String t) -> t.equals("--" + b.getKey().toLowerCase().trim()))
                )
                .forEach((Map.Entry<String, Bundle> t) -> {
                    LOGGER.info("Found bundle " + t.getValue().name() + " in " + t.getValue().getClass());
                    bundles.add(t.getValue());
                });
        LOGGER.info("Bundles discovery complete");
        LOGGER.info("Starting bundles...");
        for (Bundle b : bundles) {
            LOGGER.info("Starting bundle " + b.name());
            b.start();
            LOGGER.info("Bundle " + b.name() + " started");
        }
        LOGGER.info("Bundles started");
    }

    @Bean
    public ApplicationListener<ApplicationEvent> hook() {
        LOGGER.info("BundleHook registered");
        return (ApplicationEvent event) -> {
            if (event instanceof ContextClosedEvent) {
                LOGGER.info("Stopping bundles...");
                bundles.forEach((Bundle b) -> {
                    LOGGER.info("Stopping bundle " + b.name());
                    try {
                        b.stop();
                        LOGGER.info("Bundle " + b.name() + " stopped");
                    } catch (Exception ex) {
                        LOGGER.warn("Could not stop bundle " + b.name(), ex);
                    }
                });
                LOGGER.info("Bundles stopped");
            }
        };
    }

}
