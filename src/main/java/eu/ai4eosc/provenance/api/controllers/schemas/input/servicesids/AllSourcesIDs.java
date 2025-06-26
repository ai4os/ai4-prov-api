package eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids;

public record AllSourcesIDs(String applicationId, JenkinsWorkflowId jenkinsWorkflow, MLFlowRunId mlflowRun,
                            NomadDeploymentId nomadDeployment) {
}
