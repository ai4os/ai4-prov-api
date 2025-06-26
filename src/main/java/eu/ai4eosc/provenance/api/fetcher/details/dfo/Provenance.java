package eu.ai4eosc.provenance.api.fetcher.details.dfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Provenance(
        @JsonProperty("nomad_job")
        String nomadDepId,
        @JsonProperty("mlflow_run")
        String mlflowRun) { }