package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins;

import java.net.URL;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
@JsonSerialize
public record JenkinsWorkflow (
		String origin,
		String applicationId,
		Id id,
		String label,
		String fullLabel,
		URL jenkinsUrl,
		URL gitUrl,
		Trace trace) {

    public JenkinsWorkflow(Id id, String applicationId, String label, String fullLabel, URL jenkinsUrl, URL gitUrl, Trace trace) {
		this("Jenkins", applicationId, id, label, fullLabel, jenkinsUrl, gitUrl, trace);
	}

	public String getUniqueId() {
		return "%s/%s/%s/%s".formatted(id.jobGroup(), id.jobBranch(), id.jobName(), id.buildId());
	}
}
