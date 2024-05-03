package de.qaware.security.lab.workloadid.middleware.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController("/api")
public class SampleResource {
    private static final Logger LOGGER = LogManager.getLogger(SampleResource.class);

    private final WebClient restTemplate;

    @Value("${hello.backend.url}")
    private String helloBackendUrl;

    @Autowired
    public SampleResource(WebClient restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/hello")
    public String hello() {
        LOGGER.info("Try to get the backend answer from {} to say hello.", helloBackendUrl);
        String backend = restTemplate.get()
            .uri(helloBackendUrl)
            .retrieve()
            .toEntity(String.class)
            .block()
            .getBody();
        return "Hello " + backend + "!";
    }
}
