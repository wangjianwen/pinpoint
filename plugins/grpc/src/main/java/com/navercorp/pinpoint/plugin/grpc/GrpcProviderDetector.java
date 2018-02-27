package com.navercorp.pinpoint.plugin.grpc;


import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Jianwen Wang
 */
public class GrpcProviderDetector implements ApplicationTypeDetector {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public ServiceType getApplicationType() {
        return GrpcConstants.GRPC_SERVER;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        return provider.checkForClass("ServerCalls$UnaryServerCallHandler");
    }
}