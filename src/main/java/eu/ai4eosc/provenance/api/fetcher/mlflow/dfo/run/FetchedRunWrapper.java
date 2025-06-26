package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FetchedRunWrapper {
    String applicationId;
    @JsonUnwrapped
    FetchedRun run;
}
