package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

public class K8sOAuth2ClientCredentialsGrantRequestEntityConverter extends AbstractK8sOAuth2RequestEntityConverter<OAuth2ClientCredentialsGrantRequest> {

    public K8sOAuth2ClientCredentialsGrantRequestEntityConverter(Path serviceAccountTokenPath) {
        super(serviceAccountTokenPath);
    }

    @Override
    protected MultiValueMap<String, String> getGrantSpecificParameters(OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        if (!CollectionUtils.isEmpty(clientCredentialsGrantRequest.getClientRegistration().getScopes())) {
            parameters.add(OAuth2ParameterNames.SCOPE,
                StringUtils.collectionToDelimitedString(clientCredentialsGrantRequest.getClientRegistration().getScopes(), " "));
        }
        return parameters;
    }
}
