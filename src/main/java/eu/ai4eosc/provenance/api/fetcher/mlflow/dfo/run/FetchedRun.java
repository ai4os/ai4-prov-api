package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record FetchedRun(
        Info info,
        Data data,
        JsonNode inputs
) {
    public record Info(
            @JsonProperty("run_uuid")
            String runUUID,
            @JsonProperty("experiment_id")
            String experimentId,
            @JsonProperty("run_name")
            String runName,
            @JsonProperty("user_id")
            String userId,
            String status,
            @JsonProperty("start_time")
            Long startTime,
            @JsonProperty("end_time")
            Long endTime,
            @JsonProperty("artifact_uri")
            String artifactURI,
            @JsonProperty("lifecycle_stage")
            String lifecycleStage,
            @JsonProperty("run_id")
            String runId
    ) {
    }

    public record Data(List<Metric> metrics,
                       List<KeyValue> params,
                       List<KeyValue> tags) {
    }
}
