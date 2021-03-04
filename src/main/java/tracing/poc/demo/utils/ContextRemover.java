package tracing.poc.demo.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import tracing.poc.demo.config.OpenTelemetrySingleton;

import javax.servlet.http.HttpServletRequest;

public class ContextRemover {


    private static final TextMapPropagator.Getter<HttpServletRequest> getter =
            new TextMapPropagator.Getter<>() {
                @Override
                public Iterable<String> keys(HttpServletRequest carrier) {
                    return (Iterable<String>) carrier.getHeaderNames();
                }

                @Override
                public String get(HttpServletRequest carrier, String key) {
                    return carrier.getHeader(key);
                }
            };

    public static Context ExtractContext(HttpServletRequest request) {
        Context context = OpenTelemetrySingleton.getInstance().getOpenTelemetry()
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, getter);

        Span span =
                OpenTelemetrySingleton.getInstance().getOpenTelemetry()
                        .getTracer(request.getServletPath())
                        .spanBuilder(request.getRequestURI())
                        .setParent(context)
                        .setSpanKind(Span.Kind.SERVER)
                        .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Set the Semantic Convention
            span.setAttribute("component", "http");
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.scheme", "http");
            span.setAttribute("http.target", request.getServletPath());
            return Context.current();
        } finally {
            // Close the span
            span.end();
        }
    }
}
