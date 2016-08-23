package com.baidu.stock.quote.process.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.baidu.stock.process.monitor.ext.InstrumentedAppender;
import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;


public class InstrumentedAppender1Test {
	final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
	final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    

    @Before
    public void setUp() throws Exception {
    	appender.setContext(root.getLoggerContext());
    	root.addAppender(appender);
    	appender.start();
    }

    @Test
    public void metersTraceEvents() throws Exception {
    	org.slf4j.Logger logger=LoggerFactory.getLogger(InstrumentedAppender1Test.class);
    	logger.info("vvvvvvvvvvvvvvv");
    	logger.error("dd");
    	System.out.println(registry.meter("ch.qos.logback.core.Appender.all").getCount());
    	System.out.println(registry.meter("ch.qos.logback.core.Appender.error").getCount());
    }

}
