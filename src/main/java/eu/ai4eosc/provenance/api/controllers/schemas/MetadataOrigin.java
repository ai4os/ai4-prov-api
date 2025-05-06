package eu.ai4eosc.provenance.api.controllers.schemas;

import eu.ai4eosc.provenance.api.entities.JenkinsWorkflowRequest;
import eu.ai4eosc.provenance.api.entities.MLFlowExperiment;
import eu.ai4eosc.provenance.api.entities.NomadJobRequest;

public record MetadataOrigin(
        String applicationId,
        MLFlowExperiment mlflowExperiment,
        JenkinsWorkflowRequest jenkinsWorkflow,
        NomadJobRequest nomadJob
) { }
