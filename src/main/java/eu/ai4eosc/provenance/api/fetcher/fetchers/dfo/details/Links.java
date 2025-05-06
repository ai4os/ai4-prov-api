package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record Links(
        @JsonProperty("source_code")
        String sourceCode,
        @JsonProperty("docker_image")
        String dockerImage,
        @JsonProperty("ai4_template")
        String ai4Template,
        @JsonProperty("cicd_url")
        String cicdUrl,
        @JsonProperty("cicd_badge")
        String cicdBadge
) {
}