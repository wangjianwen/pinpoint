package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import io.grpc.Metadata;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author Jianwen Wang
 */
public interface GrpcConstants {
    ServiceType GRPC_SERVER = ServiceTypeFactory.of(1911, "GRPC_SERVER", RECORD_STATISTICS);
    ServiceType GRPC_CLIENT = ServiceTypeFactory.of(9911, "GRPC_CLIENT", RECORD_STATISTICS);
    AnnotationKey GRPC_ARGS = AnnotationKeyFactory.of(995, "GRPC_ARGS", VIEW_IN_RECORD_SET);
    AnnotationKey GRPC_RESULT = AnnotationKeyFactory.of(998, "GRPC_RESULT", VIEW_IN_RECORD_SET);
}