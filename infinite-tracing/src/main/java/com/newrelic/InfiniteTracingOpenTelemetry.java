package com.newrelic;

import com.google.common.annotations.VisibleForTesting;
import com.newrelic.agent.model.SpanEvent;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.MetricAggregator;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class InfiniteTracingOpenTelemetry implements InfiniteTracing {
    private final Logger logger;
    private final InfiniteTracingConfig config;
    private final MetricAggregator aggregator;
    private final ExecutorService executorService;
    private final BlockingQueue<SpanEvent> queue;

    @VisibleForTesting
    InfiniteTracingOpenTelemetry(InfiniteTracingConfig config, MetricAggregator aggregator, ExecutorService executorService, BlockingQueue<SpanEvent> queue) {
        this.logger = config.getLogger();
        this.config = config;
        this.aggregator = aggregator;
        this.executorService = executorService;
        this.queue = queue;
    }

    static InfiniteTracingOpenTelemetry initialize(InfiniteTracingConfig config, MetricAggregator aggregator) {
        ExecutorService executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Infinite Tracing"));
        return new InfiniteTracingOpenTelemetry(config, aggregator, executorService, new LinkedBlockingDeque<SpanEvent>(config.getMaxQueueSize()));
    }
    @Override
    public void accept(SpanEvent x) {

    }

    @Override
    public void start(String agentRunToken, Map<String, String> requestMetadata) {

    }

    @Override
    public void stop() {

    }
}
