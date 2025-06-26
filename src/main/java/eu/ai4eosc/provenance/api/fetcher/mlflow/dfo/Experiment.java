package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run.FetchedRunWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Experiment {
		String origin;
		String applicationId;
		@JsonUnwrapped
		FetchedExperiment fetchedExperiment;
		FetchedRunWrapper run;
}
