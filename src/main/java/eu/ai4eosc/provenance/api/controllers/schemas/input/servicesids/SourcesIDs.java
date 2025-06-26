package eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids;

public record SourcesIDs(
        String applicationId,
        JenkinsWorkflowId jenkinsWorkflow
) { }
