package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import io.grpc.Metadata;

public class ClientCallImplInterceptor implements AroundInterceptor2 {

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public ClientCallImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg1, Object arg2) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        Metadata metadata = (Metadata) arg2;
        metadata.put(GrpcConstants.META_TRANSACTION_ID, nextId.getTransactionId());
        metadata.put(GrpcConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
        metadata.put(GrpcConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
        metadata.put(GrpcConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
        metadata.put(GrpcConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
        metadata.put(GrpcConstants.META_FLAGS, Short.toString(nextId.getFlags()));
    }

    @IgnoreMethod
    @Override
    public void after(Object o, Object arg1, Object arg2, Object result, Throwable throwable) {
    }
}