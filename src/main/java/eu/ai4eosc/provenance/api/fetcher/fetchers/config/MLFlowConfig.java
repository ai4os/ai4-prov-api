package eu.ai4eosc.provenance.api.fetcher.fetchers.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
@ConfigurationProperties(prefix = "mlflow")
public record MLFlowConfig(URL host, MLFlowCredentials credentials) {
    public record MLFlowCredentials(String username, String password) {
    }
}