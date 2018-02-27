package com.navercorp.pinpoint.plugin.grpc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.GrpcMetadataKeyConstants;
import io.grpc.Metadata;
import io.grpc.ServerCall;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * @author Jianwen Wang
 */
public class ServerCallsInterceptor extends SpanSimpleAroundInterceptor {

    public ServerCallsInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ServerCallsInterceptor.class);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Metadata metadata = (Metadata) args[1];

        String transactionId = (String) metadata.get(GrpcMetadataKeyConstants.META_TRANSACTION_ID);

        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        long parentSpanId = NumberUtils.parseLong(metadata.get(GrpcMetadataKeyConstants.META_PARENT_SPAN_ID).toString(), SpanId.NULL);
        long spanId = NumberUtils.parseLong(metadata.get(GrpcMetadataKeyConstants.META_SPAN_ID).toString(), SpanId.NULL);
        short flags = NumberUtils.parseShort(metadata.get(GrpcMetadataKeyConstants.META_FLAGS).toString(), (short) 0);

        TraceId traceId = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
        return traceContext.continueTraceObject(traceId);
    }

    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        Metadata headers = (Metadata) args[1];
        ServerCall call = (ServerCall) args[0];
        final String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        recorder.recordServiceType(GrpcConstants.GRPC_SERVER);
        recorder.recordRpcName(fullMethodName);

        io.netty.channel.Channel channel = getChannelByReflect(call);
        if(channel != null) {
            final InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
            recorder.recordEndPoint(localAddress.getHostName());
            final InetSocketAddress remoteAddress = (InetSocketAddress) channel.localAddress();
            recorder.recordRemoteAddress(remoteAddress.getHostName() + ":" + remoteAddress.getPort());

            if (!recorder.isRoot()) {
                String parentApplicationName = headers.get(GrpcMetadataKeyConstants.META_PARENT_APPLICATION_NAME).toString();

                if (parentApplicationName != null) {
                    short parentApplicationType = NumberUtils.parseShort(
                            headers.get(GrpcMetadataKeyConstants.META_PARENT_APPLICATION_TYPE).toString(),
                            ServiceType.UNDEFINED.getCode());
                    recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                    if (channel != null) {
                        recorder.recordAcceptorHost(remoteAddress.getHostName() + ":" + remoteAddress.getPort());
                    }
                }
            }
        }
    }

    private io.netty.channel.Channel getChannelByReflect(ServerCall call) {
        try {
            final Field streamField = call.getClass().getDeclaredField("stream");
            streamField.setAccessible(true);
            final Object stream = streamField.get(call);
            final Field channelField = stream.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            return (io.netty.channel.Channel) channelField.get(stream);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object o1, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
    }
}