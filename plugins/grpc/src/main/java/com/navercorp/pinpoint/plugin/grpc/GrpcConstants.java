package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import io.grpc.Metadata;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public interface GrpcConstants {
    //INCLUDE_DESTINATION_ID:可以在调用栈的api显示栏显示出ip:port
    // RECORD_STATISTICS:未知功能
    // TERMINAL:加这个属性会导致调用栈不显示

    ServiceType GRPC_SERVER = ServiceTypeFactory.of(1911, "GRPC_SERVER", RECORD_STATISTICS);
    ServiceType GRPC_CLIENT = ServiceTypeFactory.of(9911, "GRPC_CLIENT", RECORD_STATISTICS);
    /**
     * 这个args是在web的调用栈中显示接收的参数的
     */
    AnnotationKey GRPC_ARGS = AnnotationKeyFactory.of(995, "GRPC_ARGS", VIEW_IN_RECORD_SET);
    /* * 这个原本是想让其在web的调用栈中显示出返回的结果,但是目前未实现 */
    AnnotationKey GRPC_RESULT = AnnotationKeyFactory.of(998, "grpc.result", VIEW_IN_RECORD_SET);


    Metadata.Key META_TRANSACTION_ID = Metadata.Key.of("_GRPC_TRASACTION_ID", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key META_SPAN_ID = Metadata.Key.of("_GRPC_SPAN_ID", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key META_PARENT_SPAN_ID = Metadata.Key.of("_GRPC_PARENT_SPAN_ID", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key META_PARENT_APPLICATION_NAME = Metadata.Key.of("_GRPC_PARENT_APPLICATION_NAME", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key META_PARENT_APPLICATION_TYPE = Metadata.Key.of("_GRPC_PARENT_APPLICATION_TYPE", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key META_FLAGS = Metadata.Key.of("_GRPC_FLAGS", Metadata.ASCII_STRING_MARSHALLER);
}