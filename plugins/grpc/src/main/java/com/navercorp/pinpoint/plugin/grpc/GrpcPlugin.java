package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate; import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware; import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy; import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope; import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin; import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

public class GrpcPlugin implements ProfilerPlugin, TransformTemplateAware {
    private TransformTemplate transformTemplate;

    private static final String GRPC_CLIENT_SCOPE = "grpc_client_scope";
    private static final String GRPC_SERVER_SCOPE = "grpc_server_scope";


    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addApplicationTypeDetector(context);

        addClient();
        addServer();
    }

    private void addClient () {
        transformTemplate.transform("io.grpc.stub.ClientCalls",
                new TransformCallback() {
                    @Override
                    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                        InterceptorScope scope = instrumentor.getInterceptorScope(GRPC_CLIENT_SCOPE);
                        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                        target.getDeclaredMethod("blockingUnaryCall", "io.grpc.Channel", "io.grpc.MethodDescriptor",
                                "io.grpc.CallOptions", "java.lang.Object")
                                .addScopedInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.ClientCallsInterceptor", scope);
                        return target.toBytecode();
                    }
                });

            transformTemplate.transform("io.grpc.internal.ClientCallImpl", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InterceptorScope scope = instrumentor.getInterceptorScope(GRPC_CLIENT_SCOPE);
                    InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    target.getDeclaredMethod("start", "io.grpc.ClientCall$Listener", "io.grpc.Metadata")
                            .addScopedInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.ClientCallImplInterceptor", scope, ExecutionPolicy.INTERNAL);
                    return target.toBytecode();
                }
            });
        }
        private void addServer(){
            transformTemplate.transform("io.grpc.stub.ServerCalls$UnaryServerCallHandler",
                    new TransformCallback() {
                        @Override
                        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                            InterceptorScope scope = instrumentor.getInterceptorScope(GRPC_SERVER_SCOPE);
                            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                            target.getDeclaredMethod("startCall", "io.grpc.ServerCall", "io.grpc.Metadata")
                                    .addScopedInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.ServerCallsInterceptor", scope);
                            return target.toBytecode();
                        }
                    });
        }

        private void addApplicationTypeDetector(ProfilerPluginSetupContext context) {
            context.addApplicationTypeDetector(new GrpcProviderDetector());
        }

        @Override
        public void setTransformTemplate(TransformTemplate transformTemplate) {
            this.transformTemplate = transformTemplate;
        }
    }