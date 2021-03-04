package tracing.poc.demo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tracing.poc.demo.config.OpenTelemetrySingleton;


@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final Logger LOG =
            LoggerFactory.getLogger(AppStartupRunner.class);

    @Value("${jaeger.host}")
    private String jaegerHost;

    @Value("${jaeger.port}")
    private int jaegerPort;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        OpenTelemetrySingleton.init(initOpenTelemetry(jaegerHost, jaegerPort)) ;

    }

    static OpenTelemetry initOpenTelemetry(String jaegerHost, int jaegerPort) {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel =
                ManagedChannelBuilder.forAddress(jaegerHost, jaegerPort).usePlaintext().build();
        // Export traces to Jaeger
        JaegerGrpcSpanExporter jaegerExporter =
                JaegerGrpcSpanExporter.builder().setServiceName("java-demo").setDeadlineMs(30000)
                        .setChannel(jaegerChannel)
                        .build();

        Resource serviceNameResource =
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "java-demo"));

        // Set to process the spans by the Jaeger Exporter
        BatchSpanProcessor batchSpansProcessor =
                BatchSpanProcessor.builder(jaegerExporter).build();

        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .setTraceConfig(TraceConfig.getDefault())
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();
        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder().setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .setTracerProvider(tracerProvider).build();
         openTelemetry.getTracerManagement().addSpanProcessor(batchSpansProcessor);
        // it's always a good idea to shut down the SDK cleanly at JVM exit.
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::shutdown));

        return openTelemetry;
    }
}
