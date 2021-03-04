package tracing.poc.demo.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestLine;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.opentelemetry.context.Context;

import java.util.Map;

public interface TestClient {

    static TestClient create(String url) {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .requestInterceptor(new RequestInterceptorTelemetry())
                .client(new ApacheHttpClient())
                .target(TestClient.class, url);

    }
    static TestClient create(String url, Context ctx) {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .requestInterceptor(new RequestInterceptorTelemetry(ctx))
                .client(new ApacheHttpClient())
                .target(TestClient.class, url);

    }

    @RequestLine("GET /test")
    Map<String, Object> test1();

    @RequestLine("GET /test2")
    Map<String, Object> test2();



}
