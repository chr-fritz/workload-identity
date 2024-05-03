package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class KubernetesOAuth2AuthorizationCodeGrantRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

    public static final String CLIENT_ASSERTATION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final Path serviceAccountTokenPath;

    public KubernetesOAuth2AuthorizationCodeGrantRequestEntityConverter(Path serviceAccountTokenPath) {
        this.serviceAccountTokenPath = serviceAccountTokenPath;
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        HttpHeaders headers = getDefaultTokenRequestHeaders();
        MultiValueMap<String, String> parameters = getParameters(authorizationGrantRequest);
        URI uri = UriComponentsBuilder
            .fromUriString(authorizationGrantRequest.getClientRegistration().getProviderDetails().getTokenUri())
            .build()
            .toUri();
        return new RequestEntity<>(parameters, headers, HttpMethod.POST, uri);
    }

    private MultiValueMap<String, String> getParameters(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        try {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

            String clientAssertation = Files.readString(serviceAccountTokenPath, StandardCharsets.UTF_8);

            OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();

            parameters.add(OAuth2ParameterNames.GRANT_TYPE, authorizationGrantRequest.getGrantType().getValue());
            parameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());

            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTATION_TYPE_JWT_BEARER);
            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION, clientAssertation);

            String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();
            if (redirectUri != null) {
                parameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
            }

            String codeVerifier = authorizationExchange.getAuthorizationRequest()
                .getAttribute(PkceParameterNames.CODE_VERIFIER);
            if (codeVerifier != null) {
                parameters.add(PkceParameterNames.CODE_VERIFIER, codeVerifier);
            }
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
