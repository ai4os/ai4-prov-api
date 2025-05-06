package eu.ai4eosc.provenance.api.fetcher.fetchers;

public interface DetailsProvider {
    public Object fetch(String applicationId) throws Exception;
}
