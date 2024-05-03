package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.file.Path;

public class K8sOAuth2AuthorizationCodeGrantRequestEntityConverter extends AbstractK8sOAuth2RequestEntityConverter<OAuth2AuthorizationCodeGrantRequest> {

    public K8sOAuth2AuthorizationCodeGrantRequestEntityConverter(Path serviceAccountTokenPath) {
        super(serviceAccountTokenPath);
    }

    @Override
    protected MultiValueMap<String, String> getGrantSpecificParameters(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();

        parameters.add(OAuth2ParameterNames.GRANT_TYPE, authorizationGrantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());
        parameters.add(OAuth2ParameterNames.SCOPE, String.join(" ", authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest().getScopes()));

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
    }
}
