package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.TraceInfo;
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

        final TraceInfo traceInfo = (TraceInfo) scope.getCurrentInvocation().getAttachment();
        Metadata metadata = (Metadata) arg2;
        metadata.put(Metadata.Key.of(GrpcConstants.META_TRANSACTION_ID, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getTransactionId());
        metadata.put(Metadata.Key.of(GrpcConstants.META_SPAN_ID, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getSpanId());
        metadata.put(Metadata.Key.of(GrpcConstants.META_PARENT_SPAN_ID, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getParentSpanId());
        metadata.put(Metadata.Key.of(GrpcConstants.META_PARENT_APPLICATION_TYPE, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getApplicationType());
        metadata.put(Metadata.Key.of(GrpcConstants.META_PARENT_APPLICATION_NAME, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getApplicationName());
        metadata.put(Metadata.Key.of(GrpcConstants.META_FLAGS, Metadata.ASCII_STRING_MARSHALLER),
                traceInfo.getMetaFlags());
    }

    @IgnoreMethod
    @Override
    public void after(Object o, Object arg1, Object arg2, Object result, Throwable throwable) {
    }
}