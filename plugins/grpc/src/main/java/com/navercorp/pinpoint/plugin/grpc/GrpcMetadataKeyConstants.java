package com.navercorp.pinpoint.plugin.grpc;

import io.grpc.Metadata;

/**
 * @author Jianwen Wang
 */
public interface GrpcMetadataKeyConstants {

    /**
     * transaction_id key in metadata
      */
    Metadata.Key META_TRANSACTION_ID = Metadata.Key.of("_GRPC_TRASACTION_ID", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * span_id key in metadata
     */
    Metadata.Key META_SPAN_ID = Metadata.Key.of("_GRPC_SPAN_ID", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * parent_span_id key in metadata
     */
    Metadata.Key META_PARENT_SPAN_ID = Metadata.Key.of("_GRPC_PARENT_SPAN_ID", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * parent_application_name key in metadata
     */
    Metadata.Key META_PARENT_APPLICATION_NAME = Metadata.Key.of("_GRPC_PARENT_APPLICATION_NAME", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * parent_application_type key in metadata
     */
    Metadata.Key META_PARENT_APPLICATION_TYPE = Metadata.Key.of("_GRPC_PARENT_APPLICATION_TYPE", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * flags key in metadata
     */
    Metadata.Key META_FLAGS = Metadata.Key.of("_GRPC_FLAGS", Metadata.ASCII_STRING_MARSHALLER);
}
