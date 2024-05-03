package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

public class K8sOAuth2RefreshTokenGrantRequestEntityConverter extends AbstractK8sOAuth2RequestEntityConverter<OAuth2RefreshTokenGrantRequest> {

    public K8sOAuth2RefreshTokenGrantRequestEntityConverter(Path serviceAccountTokenPath) {
        super(serviceAccountTokenPath);
    }

    @Override
    protected MultiValueMap<String, String> getGrantSpecificParameters(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        parameters.add(OAuth2ParameterNames.GRANT_TYPE, refreshTokenGrantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.REFRESH_TOKEN, refreshTokenGrantRequest.getRefreshToken().getTokenValue());
        if (!CollectionUtils.isEmpty(refreshTokenGrantRequest.getScopes())) {
            parameters.add(OAuth2ParameterNames.SCOPE,
                StringUtils.collectionToDelimitedString(refreshTokenGrantRequest.getScopes(), " "));
        }

        return parameters;
    }
}
