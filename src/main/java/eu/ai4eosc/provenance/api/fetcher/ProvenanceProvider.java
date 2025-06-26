package eu.ai4eosc.provenance.api.fetcher;

public interface ProvenanceProvider {
	public Object fetch(Record id, String applicationId) throws Exception;
}
