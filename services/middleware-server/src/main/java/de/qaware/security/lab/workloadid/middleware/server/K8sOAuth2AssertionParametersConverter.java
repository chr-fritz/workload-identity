package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class K8sOAuth2AssertionParametersConverter<T extends AbstractOAuth2AuthorizationGrantRequest>
    implements Converter<T, MultiValueMap<String, String>> {

    public static final String CLIENT_ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final Path serviceAccountTokenPath;

    K8sOAuth2AssertionParametersConverter(Path serviceAccountTokenPath) {
        this.serviceAccountTokenPath = serviceAccountTokenPath;
    }

    @Override
    public MultiValueMap<String, String> convert(T authorizationGrantRequest) {
        try {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

            String clientAssertion = Files.readString(serviceAccountTokenPath, StandardCharsets.UTF_8);

            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_JWT_BEARER);
            parameters.add(OAuth2ParameterNames.CLIENT_ASSERTION, clientAssertion);

            return parameters;
        } catch (IOException e) {
            OAuth2Error oauth2Error = new OAuth2Error(
                "missing_client_assertion_jwt",
                "An error occurred while attempting to prepare the OAuth 2.0 Access Token request: " + e.getMessage(),
                null
            );
            throw new OAuth2AuthorizationException(oauth2Error, e);
        }
    }
}
