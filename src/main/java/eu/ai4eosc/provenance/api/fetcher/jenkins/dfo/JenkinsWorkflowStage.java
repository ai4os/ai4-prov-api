package eu.ai4eosc.provenance.api.fetcher.jenkins.dfo;

import java.util.Map;

public record JenkinsWorkflowStage(Id jenkins,
                                   String id,
                                   String name,
                                   Long startTime,
                                   Long endTime,
                                   Long duration,
                                   Map<String, Object> properties) {}
