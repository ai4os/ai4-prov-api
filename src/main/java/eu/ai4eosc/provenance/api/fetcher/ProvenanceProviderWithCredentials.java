package eu.ai4eosc.provenance.api.fetcher;

import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.ICredentials;

public interface ProvenanceProviderWithCredentials {
    public Object fetch(ICredentials credentials, Record id, String applicationId) throws Exception;
}
