package eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids;

public record JenkinsWorkflowId(String id, String name, String group, String branch, Integer build) {}