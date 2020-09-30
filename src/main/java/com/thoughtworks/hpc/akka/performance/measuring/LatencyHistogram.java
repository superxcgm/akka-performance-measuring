package com.thoughtworks.hpc.akka.performance.measuring;

import org.HdrHistogram.Histogram;

public class LatencyHistogram extends Histogram {
    private long t = 0;

    public LatencyHistogram() {
        super(1000_000_000L, 2);
    }

    public void record() {
        long t1 = System.nanoTime();
        if (t != 0) {
            recordValue(t1 - t);
        }
        t = t1;
    }
}
