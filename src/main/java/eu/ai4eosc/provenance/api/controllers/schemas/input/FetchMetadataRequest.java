package eu.ai4eosc.provenance.api.controllers.schemas.input;

import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.Credentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.SourcesIDs;

public record FetchMetadataRequest(SourcesIDs sources, Credentials credentials) {
}
