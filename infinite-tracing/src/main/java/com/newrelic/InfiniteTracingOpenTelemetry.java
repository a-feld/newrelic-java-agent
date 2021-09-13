package com.newrelic;

import com.google.common.annotations.VisibleForTesting;
import com.newrelic.agent.model.SpanEvent;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.MetricAggregator;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;

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

    /**
     * Offer the span event to the queue to be written to the Infinite Trace Observer. If the queue
     * is at capacity, the span event is ignored.
     *
     * @param spanEvent the span event
     */
    @Override
    public void accept(SpanEvent spanEvent) {
        aggregator.incrementCounter("Supportability/InfiniteTracing/Span/Seen");
        if (!queue.offer(spanEvent)) {
            logger.log(Level.FINEST, "Span event not accepted. The queue was full.");
        }
    }

    @Override
    public void start(String agentRunToken, Map<String, String> requestMetadata) {

    }

    @Override
    public void stop() {

    }
}
