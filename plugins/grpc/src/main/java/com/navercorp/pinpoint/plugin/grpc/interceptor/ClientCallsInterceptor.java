package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import io.grpc.Channel;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * @author Jianwen Wang
 */
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
    }

    @Override
    public void after(Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object result,
                      Throwable throwable) {

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        recorder.recordApi(descriptor);

        Channel channel = (Channel) arg1;
        io.grpc.MethodDescriptor method = (io.grpc.MethodDescriptor) arg2;
        try {

            if (throwable == null) {
                InetSocketAddress remoteAddr = getInetSocketAddressByReflect(channel);
                if(remoteAddr != null){
                    recorder.recordEndPoint(remoteAddr.getHostName());
                    recorder.recordDestinationId(remoteAddr.getHostName() + ":" + remoteAddr.getPort());
                }
                recorder.recordRpcName(method.getFullMethodName());
                recorder.recordAttribute(GrpcConstants.GRPC_ARGS, arg4);
                recorder.recordAttribute(GrpcConstants.GRPC_RESULT, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private InetSocketAddress getInetSocketAddressByReflect(Channel channel){
        try {
            final Field targetField = channel.getClass().getDeclaredField("target");
            targetField.setAccessible(true);
            String remoteAddr = targetField.get(channel).toString();
            int index = remoteAddr.indexOf(":");
            String host = remoteAddr.substring(0, index);
            int port = Integer.valueOf(remoteAddr.substring(index + 1));
            return new InetSocketAddress(host, port);
        } catch (Exception e){
            // 打印日志
            return null;
        }
    }
}