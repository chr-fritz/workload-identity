package de.qaware.security.lab.workloadid.middleware.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class ConfigProperties {
    private Path serviceAccountTokenPath;
    private boolean useKubernetesServiceAccount;
}
