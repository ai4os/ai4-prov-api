package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KeyRunId {
    String runId;
    String key;
}