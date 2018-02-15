package com.navercorp.pinpoint.plugin.grpc;


import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class GrpcTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(GrpcConstants.GRPC_SERVER);
        context.addServiceType(GrpcConstants.GRPC_CLIENT);
        context.addAnnotationKey(GrpcConstants.GRPC_ARGS);
        context.addAnnotationKey(GrpcConstants.GRPC_RESULT);
    }
}