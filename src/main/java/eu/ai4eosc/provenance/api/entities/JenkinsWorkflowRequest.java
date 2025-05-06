package eu.ai4eosc.provenance.api.entities;

// In future execution probably won't be necessary because we will take the latest execution?
public record JenkinsWorkflowRequest(String id, String jobName, String jobGroup, String jobBranch, Integer execution) {
    private JenkinsWorkflowRequest(String jobName, String jobGroup, String jobBranch, Integer execution) {
        this("%s-%s-%s-%d".formatted(jobName, jobGroup, jobBranch, execution),
                jobName, jobGroup, jobBranch, execution);
    }
}