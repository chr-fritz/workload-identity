package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

abstract class AbstractK8sOAuth2RequestEntityConverter<T extends AbstractOAuth2AuthorizationGrantRequest> implements Converter<T, RequestEntity<?>> {

    public static final String CLIENT_ASSERTATION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final Path serviceAccountTokenPath;

    protected AbstractK8sOAuth2RequestEntityConverter(Path serviceAccountTokenPath) {
        this.serviceAccountTokenPath = serviceAccountTokenPath;
    }

    @Override
    public RequestEntity<?> convert(T authorizationGrantRequest) {
        HttpHeaders headers = getDefaultTokenRequestHeaders();
        MultiValueMap<String, String> parameters = getParameters(authorizationGrantRequest);
        URI uri = UriComponentsBuilder
            .fromUriString(authorizationGrantRequest.getClientRegistration().getProviderDetails().getTokenUri())
            .build()
            .toUri();
        return new RequestEntity<>(parameters, headers, HttpMethod.POST, uri);
    }

    protected abstract MultiValueMap<String, String> getGrantSpecificParameters(T authorizationGrantRequest);

    private MultiValueMap<String, String> getParameters(T authorizationGrantRequest) {
        try {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

            String clientAssertation = Files.readString(serviceAccountTokenPath, StandardCharsets.UTF_8);

            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTATION_TYPE_JWT_BEARER);
            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION, clientAssertation);

            parameters.addAll(getGrantSpecificParameters(authorizationGrantRequest));

            return parameters;
        } catch (IOException e) {
            OAuth2Error oauth2Error = new OAuth2Error(
                "missing_client_assertation_jwt",
                "An error occurred while attempting to prepare the OAuth 2.0 Access Token request: " + e.getMessage(),
                null
            );
            throw new OAuth2AuthorizationException(oauth2Error, e);
        }
    }

    private static HttpHeaders getDefaultTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final MediaType contentType = MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setContentType(contentType);
        return headers;
    }
}
