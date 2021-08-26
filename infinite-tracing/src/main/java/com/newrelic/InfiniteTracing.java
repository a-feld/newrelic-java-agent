package com.newrelic;

import com.newrelic.agent.interfaces.backport.Consumer;
import com.newrelic.agent.model.SpanEvent;
import com.newrelic.api.agent.MetricAggregator;

import java.util.Map;

public interface InfiniteTracing extends Consumer<SpanEvent> {
    /**
     * Initialize Infinite Tracing. Note, for spans to start being sent {@link #start(String, Map)} must
     * be called.
     *
     * @param config     the config
     * @param aggregator the metric aggregator
     * @return the instance
     */
    static InfiniteTracing initialize(InfiniteTracingConfig config, MetricAggregator aggregator) {
        if (config.getUseOtlp()) {
            return null;
        } else {
            return InfiniteTracingNewRelic.initialize(config, aggregator);
        }
    }

    /**
     * Start sending spans to the Infinite Tracing Observer. If already running, update with the
     * {@code agentRunToken} and {@code requestMetadata}.
     *
     * @param agentRunToken   the agent run token
     * @param requestMetadata any extra metadata headers that must be included
     */
    void start(String agentRunToken, Map<String, String> requestMetadata);

    /**
     * Stop sending spans to the Infinite Tracing Observer and cleanup resources. If not already running,
     * return immediately.
     */
    void stop();

}
