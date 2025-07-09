package eu.ai4eosc.provenance.api.fetcher.details.dfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FetchedDetails(
        @JsonProperty("metadata_version")
        String metadataVersion,
        String title,
        String summary,
        String description,
        String doi,
        Dates dates,
        Provenance provenance,
        Links links,
        List<String> tags,
        List<String> tasks,
        List<String> categories,
        List<String> libraries,
        @JsonProperty("data-type")
        List<String> dataTypes,
        Resources resources,
        String license,
        String id
) {
    public record Dates(String created, String updated) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Links(
            String documentation,
            @JsonProperty("source_code")
            String sourceCode,
            @JsonProperty("docker_image")
            String dockerImage,
            @JsonProperty("ai4_template")
            String ai4Template,
            String dataset,
            String weights,
            String citation,
            @JsonProperty("base_model")
            String baseModel,
            @JsonProperty("cicd_url")
            String cicdUrl,
            @JsonProperty("cicd_badge")
            String cicdBadge
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resources(Inference inference) {
        record Inference(Integer cpu,
                         Integer gpu,
                         @JsonProperty("memory_MB")
                         Integer memMB,
                         @JsonProperty("gpu_memory_MB")
                         Integer gpuMemMB,
                         @JsonProperty("gpu_compute_capability")
                         Float gpuComputeCap,
                         @JsonProperty("storage_MB")
                         Integer storageMB) {
        }
    }
}
