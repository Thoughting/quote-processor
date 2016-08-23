package com.baidu.stock.process.monitor.ext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

import static com.codahale.metrics.MetricRegistry.name;


/**
 * 日志调用监控统计
 * @author dengjianli
 *
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    private final MetricRegistry registry;

    private Meter warn;
    private Meter error;

    /**
     * Create a new instrumented appender using the given registry name.
     *
     * @param registryName the name of the registry in {@link SharedMetricRegistries}
     */
    public InstrumentedAppender(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * Create a new instrumented appender using the given registry.
     *
     * @param registry the metric registry
     */
    public InstrumentedAppender(MetricRegistry registry) {
        this.registry = registry;
        setName(Appender.class.getName());
    }

    @Override
    public void start() {
        this.warn = registry.meter(name(getName(), "warn"));
        this.error = registry.meter(name(getName(), "error"));
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        switch (event.getLevel().toInt()) {
            case Level.TRACE_INT:
                break;
            case Level.DEBUG_INT:
                break;
            case Level.INFO_INT:
                break;
            case Level.WARN_INT:
                warn.mark();
                break;
            case Level.ERROR_INT:
                error.mark();
                break;
            default:
                break;
        }
    }
}
