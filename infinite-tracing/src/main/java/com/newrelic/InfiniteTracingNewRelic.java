package com.newrelic;

import com.google.common.annotations.VisibleForTesting;
import com.newrelic.agent.model.SpanEvent;
import com.newrelic.api.agent.Logger;
import com.newrelic.api.agent.MetricAggregator;

import javax.annotation.concurrent.GuardedBy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;

public class InfiniteTracingNewRelic implements InfiniteTracing {

    private final Logger logger;
    private final InfiniteTracingConfig config;
    private final MetricAggregator aggregator;
    private final ExecutorService executorService;
    private final BlockingQueue<SpanEvent> queue;

    private final Object lock = new Object();
    @GuardedBy("lock") private Future<?> spanEventSenderFuture;
    @GuardedBy("lock") private SpanEventSender spanEventSender;
    @GuardedBy("lock") private ChannelManager channelManager;

    @VisibleForTesting
    InfiniteTracingNewRelic(InfiniteTracingConfig config, MetricAggregator aggregator, ExecutorService executorService, BlockingQueue<SpanEvent> queue) {
        this.logger = config.getLogger();
        this.config = config;
        this.aggregator = aggregator;
        this.executorService = executorService;
        this.queue = queue;
    }

    static InfiniteTracingNewRelic initialize(InfiniteTracingConfig config, MetricAggregator aggregator) {
        ExecutorService executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Infinite Tracing"));
        return new InfiniteTracingNewRelic(config, aggregator, executorService, new LinkedBlockingDeque<SpanEvent>(config.getMaxQueueSize()));
    }

    @VisibleForTesting
    ChannelManager buildChannelManager(String agentRunToken, Map<String, String> requestMetadata) {
        return new ChannelManager(config, aggregator, agentRunToken, requestMetadata);
    }

    @VisibleForTesting
    SpanEventSender buildSpanEventSender() {
        return new SpanEventSender(config, queue, aggregator, channelManager);
    }

    @Override
    public void start(String agentRunToken, Map<String, String> requestMetadata) {
        synchronized (lock) {
            if (spanEventSenderFuture != null) {
                channelManager.updateMetadata(agentRunToken, requestMetadata);
                channelManager.shutdownChannelAndBackoff(0);
                return;
            }
            logger.log(Level.INFO, "Starting Infinite Tracing.");
            channelManager = buildChannelManager(agentRunToken, requestMetadata);
            spanEventSender = buildSpanEventSender();
            spanEventSenderFuture = executorService.submit(spanEventSender);
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (spanEventSenderFuture == null) {
                return;
            }
            logger.log(Level.INFO, "Stopping Infinite Tracing.");
            spanEventSenderFuture.cancel(true);
            channelManager.shutdownChannelForever();
            spanEventSenderFuture = null;
            spanEventSender = null;
            channelManager = null;
        }
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

}