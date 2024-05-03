package de.qaware.security.lab.workloadid.middleware.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TokenForwardHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LogManager.getLogger(TokenForwardHttpRequestInterceptor.class);

    @NonNull
    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return execution.execute(request, body);
        }

        if (!(authentication.getCredentials() instanceof AbstractOAuth2Token token)) {
            return execution.execute(request, body);
        }
        LOGGER.debug("Add authorization bearer token to request to {} {}. Jwt: {}", request.getMethod(), request.getURI(), token.getTokenValue());
        request.getHeaders().setBearerAuth(token.getTokenValue());
        return execution.execute(request, body);
    }
}
