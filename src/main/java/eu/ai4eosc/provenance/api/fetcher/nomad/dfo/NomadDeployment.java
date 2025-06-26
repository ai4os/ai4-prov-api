package eu.ai4eosc.provenance.api.fetcher.nomad.dfo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NomadDeployment {
        String origin;
        String applicationId;
        @JsonUnwrapped
        FetchedNomadDeployment fetchedNomadDeployment;
}
