package de.qaware.security.lab.workloadid.middleware.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController("/api")
public class SampleResource {
    private static final Logger LOGGER = LogManager.getLogger(SampleResource.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${hello.backend.url}")
    private String helloBackendUrl;

    @GetMapping("/hello")
    public String hello() {
        LOGGER.info("Try to get the backend answer from {} to say hello.", helloBackendUrl);
        String backend = restTemplate.getForObject(helloBackendUrl, String.class);
        return "Hello " + backend + "!";
    }
}
