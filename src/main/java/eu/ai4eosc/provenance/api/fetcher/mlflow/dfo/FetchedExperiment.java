package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FetchedExperiment(
        @JsonProperty("experiment_id")
        String id,
        String name,
        @JsonProperty("artifact_location")
        String artifactLocation,
        @JsonProperty("lifecycle_stage")
        String lifecycleStage,
        @JsonProperty("last_update_time")
        Long lastUpdateTime,
        @JsonProperty("creation_time")
        Long creationTime,
        List<Tag> tags
) { }
