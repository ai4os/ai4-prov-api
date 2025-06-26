package eu.ai4eosc.provenance.api.fetcher.jenkins.dfo;

public record Id(String jobGroup,
                 String jobName,
                 String jobBranch,
                 Integer buildId) {}
