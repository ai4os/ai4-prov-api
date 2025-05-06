package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins;

public record Id(String jobGroup,
                 String jobName,
                 String jobBranch,
                 Integer buildId) {};
