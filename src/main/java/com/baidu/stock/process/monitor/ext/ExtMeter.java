package com.baidu.stock.process.monitor.ext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.terracotta.statistics.jsr166e.LongAdder;
import com.codahale.metrics.Clock;
import com.codahale.metrics.Metered;

/**
 * 修改了重置功能
 * @author dengjianli
 *
 */
public class ExtMeter implements Metered {
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final ExtEWMA m1Rate = ExtEWMA.oneMinuteEWMA();
    private final LongAdder count = new LongAdder();
    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    /**
     * Creates a new {@link Meter}.
     */
    public ExtMeter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock      the clock to use for the meter ticks
     */
    public ExtMeter(Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        tickIfNecessary();
        count.add(n);
        m1Rate.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    m1Rate.tick();
                }
            }
        }
    }

    @Override
    public long getCount() {
        return count.sum();
    }

    @Override
    public double getFifteenMinuteRate() {
        tickIfNecessary();
        return 0;
    }

    @Override
    public double getFiveMinuteRate() {
        tickIfNecessary();
        return 0;
    }

    @Override
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = (clock.getTick() - startTime);
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    @Override
    public double getOneMinuteRate() {
        tickIfNecessary();
        return m1Rate.getRate(TimeUnit.SECONDS);
    }
    
    public void reset(){
    	count.reset();
    	m1Rate.setInitialized();
    }
}
