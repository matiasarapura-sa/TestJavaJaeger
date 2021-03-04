package tracing.poc.demo.controller;

import io.opentelemetry.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tracing.poc.demo.client.TestClient;
import tracing.poc.demo.utils.ContextRemover;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<?> getResource(HttpServletRequest request)
     {
         Context ctx = ContextRemover.ExtractContext(request);

         TestClient client = TestClient.create("http://localhost:9094/",ctx);
         Map<String, Object> res = client.test1();
            return new ResponseEntity<>(res, HttpStatus.OK);

    }
    @GetMapping("/test2")
    public ResponseEntity<?> getResource1(HttpServletRequest request)
    {
        Context ctx = ContextRemover.ExtractContext(request);
        TestClient client = TestClient.create("http://localhost:9094/",ctx);
        Map<String, Object> res = client.test2();
        return new ResponseEntity<>(res, HttpStatus.OK);

    }
    @GetMapping("/health")
    public ResponseEntity<?> getResource2()
    {
        HashMap<String, String>  res = new HashMap<String,String>();
        res.put("health","alive");
        return new ResponseEntity<>(res, HttpStatus.OK);

    }
}
