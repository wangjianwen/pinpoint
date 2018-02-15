package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.TraceInfo;
import io.grpc.Channel;

import java.lang.reflect.Field;

public class ClientCallsInterceptor implements AroundInterceptor4 {

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public ClientCallsInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(GrpcConstants.GRPC_CLIENT);
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setTransactionId(nextId.getTransactionId());
        traceInfo.setSpanId(Long.toString(nextId.getSpanId()));
        traceInfo.setParentSpanId(Long.toString(nextId.getParentSpanId()));
        traceInfo.setApplicationType(Short.toString(traceContext.getServerTypeCode()));
        traceInfo.setApplicationName(traceContext.getApplicationName());
        traceInfo.setMetaFlags(Short.toString(nextId.getFlags()));

        scope.getCurrentInvocation().setAttachment(traceInfo);

    }

    @Override
    public void after(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object result,
                      Throwable throwable) {

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        Channel channel = (Channel) arg1;
        io.grpc.MethodDescriptor method = (io.grpc.MethodDescriptor) arg2;

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);

            if (throwable == null) {
                try {
                    final Field targetField = channel.getClass().getDeclaredField("target");
                    targetField.setAccessible(true);
                    String remoteAddr = targetField.get(channel).toString();
                    int index = remoteAddr.indexOf(":");
                    String host = remoteAddr.substring(0, index);
                    recorder.recordEndPoint(host);
                    recorder.recordDestinationId(remoteAddr);
                    recorder.recordRpcName(method.getFullMethodName());
                    recorder.recordAttribute(GrpcConstants.GRPC_ARGS, arg4);
                    recorder.recordAttribute(GrpcConstants.GRPC_RESULT, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}