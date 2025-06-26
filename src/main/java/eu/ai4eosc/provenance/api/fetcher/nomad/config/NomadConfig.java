package eu.ai4eosc.provenance.api.fetcher.nomad.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

@ConfigurationProperties(prefix = "nomad")
public record NomadConfig(URL host,
                          Boolean fullInfo,
                          PAPICredentials credentials) {
    public record PAPICredentials(String token, String vo) {}
}
