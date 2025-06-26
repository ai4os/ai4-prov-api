package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class KeyValue extends KeyRunId {
    String value;
}
