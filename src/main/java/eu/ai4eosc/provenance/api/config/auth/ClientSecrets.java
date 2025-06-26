package eu.ai4eosc.provenance.api.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="client")
public record ClientSecrets(String apikey) {}
