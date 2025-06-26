package eu.ai4eosc.provenance.api.fetcher.mlflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
@ConfigurationProperties(prefix = "mlflow")
public record MLFlowConfig(Origin ai4eosc, Origin imagine) {
    public record Origin (URL host) {}
}