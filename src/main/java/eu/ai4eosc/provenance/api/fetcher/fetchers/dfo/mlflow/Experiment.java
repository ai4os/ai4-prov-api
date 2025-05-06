package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.mlflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Experiment {
		String origin;
		String applicationId;
		@JsonProperty("experiment_id") String id;
		String name;
		@JsonProperty("artifact_location") String artifactLocation;
		@JsonProperty("lifecycle_stage") String lifecycleStage;
		@JsonProperty("last_update_time") Long lastUpdateTime;
		@JsonProperty("creation_time") Long creationTime;
		List<Tag> tags;
}
