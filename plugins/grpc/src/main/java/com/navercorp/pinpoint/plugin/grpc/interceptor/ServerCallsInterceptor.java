package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import io.grpc.Metadata;
import io.grpc.ServerCall;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

public class ServerCallsInterceptor extends SpanSimpleAroundInterceptor {

    public ServerCallsInterceptor(TraceContext traceContext, MethodDescriptor descriptor){
        super(traceContext, descriptor, ServerCallsInterceptor.class);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Metadata metadata = (Metadata)args[1];

        String transactionId = metadata.get(Metadata.Key.of(GrpcConstants.META_TRANSACTION_ID,
                Metadata.ASCII_STRING_MARSHALLER));

        if(transactionId == null){
            return traceContext.newTraceObject();
        }

        long parentSpanId = NumberUtils.parseLong(metadata.get(Metadata.Key.of(GrpcConstants.META_PARENT_SPAN_ID,
                Metadata.ASCII_STRING_MARSHALLER)) , SpanId.NULL);

        long spanId = NumberUtils.parseLong(metadata.get(Metadata.Key.of(GrpcConstants.META_SPAN_ID,
                Metadata.ASCII_STRING_MARSHALLER)) , SpanId.NULL);
        short flags = NumberUtils.parseShort(metadata.get(Metadata.Key.of(GrpcConstants.META_FLAGS,
                Metadata.ASCII_STRING_MARSHALLER)) ,(short) 0);

        TraceId traceId = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
        return traceContext.continueTraceObject(traceId);
    }

    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        Metadata headers = (Metadata)args[1];
        ServerCall call = (ServerCall)args[0];
        try {
            final String fullMethodName = call.getMethodDescriptor().getFullMethodName();
            final Field streamField = call.getClass().getDeclaredField("stream");
            streamField.setAccessible(true);
            final Object stream = streamField.get(call);
            final Field channelField = stream.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            final io.netty.channel.Channel channel = (io.netty.channel.Channel)channelField.get(stream);

            recorder.recordServiceType(GrpcConstants.GRPC_SERVER);
            recorder.recordRpcName(fullMethodName);
            final InetSocketAddress localAddress = (InetSocketAddress)channel.localAddress();
            recorder.recordEndPoint(localAddress.getHostName());
            final InetSocketAddress remoteAddress = (InetSocketAddress)channel.localAddress();
            recorder.recordRemoteAddress(remoteAddress.getHostName() + ":" + remoteAddress.getPort());

            if (!recorder.isRoot()) {
                String parentApplicationName = headers.get(Metadata.Key.of(GrpcConstants.META_PARENT_APPLICATION_NAME,
                        Metadata.ASCII_STRING_MARSHALLER));

                if (parentApplicationName != null) {
                    short parentApplicationType = NumberUtils.parseShort(
                            headers.get(Metadata.Key.of(GrpcConstants.META_PARENT_APPLICATION_TYPE, Metadata.ASCII_STRING_MARSHALLER)),
                            ServiceType.UNDEFINED.getCode());
                    recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                    recorder.recordAcceptorHost(remoteAddress.getHostName() + ":" + remoteAddress.getPort());
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object o1, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        //recorder.recordAttribute(SamplePluginConstants.MY_RPC_ARGUMENT_ANNOTATION_KEY, request.getArgument());
    }
}