package tracing.poc.demo.config;

import io.opentelemetry.api.OpenTelemetry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenTelemetrySingleton {
    private static OpenTelemetrySingleton ourInstance;

    private OpenTelemetry openTelemetry;

    public static OpenTelemetrySingleton getInstance() {

        return ourInstance;
    }

    public static void init(OpenTelemetry openTelemetry){
        ourInstance = new OpenTelemetrySingleton();
        ourInstance.openTelemetry = openTelemetry;
    }
    private OpenTelemetrySingleton() {
    }
}
