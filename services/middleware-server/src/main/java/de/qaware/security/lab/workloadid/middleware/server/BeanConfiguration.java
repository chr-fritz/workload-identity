package de.qaware.security.lab.workloadid.middleware.server;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Bean
    HttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @Bean
    RestTemplate rest(HttpClient httpClient, TokenForwardHttpRequestInterceptor tokenForwardInterceptor) {
        RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        rest.getInterceptors().add(tokenForwardInterceptor);
        return rest;
    }
}
