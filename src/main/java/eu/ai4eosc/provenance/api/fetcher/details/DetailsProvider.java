package eu.ai4eosc.provenance.api.fetcher.details;


public interface DetailsProvider {
    public Object fetch(String applicationId) throws Exception;
}
