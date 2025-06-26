package eu.ai4eosc.provenance.api.fetcher.jenkins.dfo;

import java.net.URL;
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
		Trace trace,
		String dockerImage) {

    public JenkinsWorkflow(Id id, String applicationId, String label, String fullLabel, URL jenkinsUrl, URL gitUrl, Trace trace, String dockerImage) {
		this("Jenkins", applicationId, id, label, fullLabel, jenkinsUrl, gitUrl, trace, dockerImage);
	}

	public String getUniqueId() {
		return "%s/%s/%s/%s".formatted(id.jobGroup(), id.jobBranch(), id.jobName(), id.buildId());
	}
}
