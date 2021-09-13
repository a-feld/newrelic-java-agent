package com.newrelic;

import com.newrelic.agent.model.SpanEvent;
import com.newrelic.trace.v1.V1;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class OpenTelemetrySpanConverter {

    private OpenTelemetrySpanConverter() {
    }

    /**
     * Convert the span event the equivalent gRPC span.
     *
     * @param spanEvent the span event
     * @return the gRPC span
     */
    static Span convert(SpanEvent spanEvent) {
        Iterable<? extends KeyValue> intrinsicAttributes = copyAttributes(spanEvent.getIntrinsics());
        Iterable<? extends KeyValue> userAttributes = copyAttributes(spanEvent.getUserAttributesCopy());
        Iterable<? extends KeyValue> agentAttributes = copyAttributes(spanEvent.getAgentAttributes());
        return Span.newBuilder().addAllAttributes(userAttributes).addAllAttributes(agentAttributes).addAllAttributes(intrinsicAttributes).build();
    }

    private static List<? extends KeyValue> copyAttributes(Map<String, Object> original) {
        List<KeyValue> copy = new LinkedList<>();
        if (original == null) {
            return copy;
        }

        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                copy.add(KeyValue.newBuilder().setKey(entry.getKey()).setValue(AnyValue.newBuilder().setStringValue((String) entry.getValue()).build()).build());
            } else if (value instanceof Long || value instanceof Integer) {
                copy.add(KeyValue.newBuilder().setKey(entry.getKey()).setValue(AnyValue.newBuilder().setIntValue((Long) entry.getValue()).build()).build());
            } else if (value instanceof Float || value instanceof Double) {
                copy.add(KeyValue.newBuilder().setKey(entry.getKey()).setValue(AnyValue.newBuilder().setDoubleValue((Double) entry.getValue()).build()).build());
            } else if (value instanceof Boolean) {
                copy.add(KeyValue.newBuilder().setKey(entry.getKey()).setValue(AnyValue.newBuilder().setBoolValue((Boolean) entry.getValue()).build()).build());
            }
        }
        return copy;
    }

}