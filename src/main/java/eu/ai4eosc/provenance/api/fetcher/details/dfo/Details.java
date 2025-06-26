package eu.ai4eosc.provenance.api.fetcher.details.dfo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Details {
        String origin;
        @JsonUnwrapped
        FetchedDetails fetchedDetails;
}
