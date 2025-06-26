package eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data()
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Metric extends KeyRunId {
    Float value;
    Long timestamp;
    Integer step;
}
