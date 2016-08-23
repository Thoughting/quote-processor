package com.baidu.stock.process.monitor.ext;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;

/**
 * 扩展historgram
 * @author dengjianli
 *
 */
public class ExtHistogram implements Metric, Sampling, Counting {
    private final Reservoir reservoir;

    public ExtHistogram(Reservoir reservoir) {
        this.reservoir = reservoir;
    }

    public void update(int value) {
        update((long) value);
    }

    public void update(long value) {
        reservoir.update(value);
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public Snapshot getSnapshot() {
        return reservoir.getSnapshot();
    }
}
