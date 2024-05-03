package de.qaware.security.lab.workloadid.middleware.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final ConfigProperties configProperties;

    public SecurityConfiguration(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(c -> c
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                .requestMatchers("/actuator/**").anonymous()
                .requestMatchers("/api", "/hello").authenticated()
            )
            .oauth2Client(withDefaults())
            .oauth2Login(withDefaults())
            .build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeAccessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient responseClient = new DefaultAuthorizationCodeTokenResponseClient();
        if (configProperties.isUseKubernetesServiceAccount()) {
            responseClient.setRequestEntityConverter(new K8sOAuth2AuthorizationCodeGrantRequestEntityConverter(configProperties.getServiceAccountTokenPath()));
        }
        return responseClient;
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenAccessTokenResponseClient() {
        DefaultRefreshTokenTokenResponseClient responseClient = new DefaultRefreshTokenTokenResponseClient();
        if (configProperties.isUseKubernetesServiceAccount()) {
            responseClient.setRequestEntityConverter(new K8sOAuth2RefreshTokenGrantRequestEntityConverter(configProperties.getServiceAccountTokenPath()));
        }
        return responseClient;
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsAccessTokenResponseClient() {
        DefaultClientCredentialsTokenResponseClient responseClient = new DefaultClientCredentialsTokenResponseClient();
        if (configProperties.isUseKubernetesServiceAccount()) {
            responseClient.setRequestEntityConverter(new K8sOAuth2ClientCredentialsGrantRequestEntityConverter(configProperties.getServiceAccountTokenPath()));
        }
        return responseClient;
    }
}
