package eu.ai4eosc.provenance.api.fetcher.fetchers;


public interface ProvenanceProvider {
	
	public Object fetch(Record id, String applicationId) throws Exception;
}
