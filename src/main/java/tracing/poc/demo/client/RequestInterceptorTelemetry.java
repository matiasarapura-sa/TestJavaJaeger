package tracing.poc.demo.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tracing.poc.demo.config.OpenTelemetrySingleton;
@AllArgsConstructor
@NoArgsConstructor
public class RequestInterceptorTelemetry implements RequestInterceptor {
    Context ctx;

    @Override
    public void apply(RequestTemplate request) {

        if (ctx != null){
            ctx.makeCurrent();
        }
        Span span = OpenTelemetrySingleton.getInstance().getOpenTelemetry().getTracer(request.path())
                .spanBuilder(request.url())
                .setSpanKind(Span.Kind.CLIENT)
                .setParent(Context.current())
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Semantic Convention.
            // (Observe that to set these, the Span does not *need* to be the current instance in Context or Scope.)
            span.setAttribute(SemanticAttributes.HTTP_METHOD, request.method());
            span.setAttribute(SemanticAttributes.HTTP_URL, request.url());
            span.setAttribute("component", "http");
            OpenTelemetrySingleton.getInstance().getOpenTelemetry().getPropagators().getTextMapPropagator().inject(Context.current(), request,  (requestTemplate, s, s1) -> {
                System.out.println(s+":" + s1);
                requestTemplate.header(s, s1);
            });

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            span.end();
        }
    }
}
