package eu.ai4eosc.provenance.api.fetcher.jenkins.dfo;

import java.net.URL;
import java.util.List;

public record Trace(Status status,
                    Long startTime,
                    Long endTime,
                    URL jenkinsUrl,
                    List<JenkinsWorkflowStage> stages,
                    List<Changeset> changesets) {}
