package eu.ai4eosc.provenance.api.fetcher.jenkins.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

@ConfigurationProperties(prefix = "jenkins")
public record JenkinsWorkflowConfig(URL host) {
}
