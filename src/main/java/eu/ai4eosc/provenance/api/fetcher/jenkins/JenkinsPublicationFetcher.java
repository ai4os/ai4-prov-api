package eu.ai4eosc.provenance.api.fetcher.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import com.cdancy.jenkins.rest.domain.job.*;
import com.cdancy.jenkins.rest.features.JobsApi;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.JenkinsWorkflowId;
import eu.ai4eosc.provenance.api.fetcher.jenkins.config.JenkinsWorkflowConfig;
import eu.ai4eosc.provenance.api.utils.URLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.Status;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.JenkinsWorkflow;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.Changeset;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.Id;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.Trace;
import eu.ai4eosc.provenance.api.fetcher.ProvenanceProvider;

@Service
public class JenkinsPublicationFetcher implements ProvenanceProvider {
    static final Logger log = LoggerFactory.getLogger(JenkinsPublicationFetcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Autowired
    private JenkinsWorkflowConfig jenkinsWorkflowConfig;

    public static class JenkinsAPINotFound extends IOException {
        public JenkinsAPINotFound(String type) {
            super("No type %s found".formatted(type));
        }
    }

    @Override
    public JenkinsWorkflow fetch(Record input, String applicationId) throws IOException, URISyntaxException {
        JenkinsWorkflowId jenkWorkflowRequest = (JenkinsWorkflowId) input;
        log.info("[!!] request jenkins {}", jenkWorkflowRequest);
        String jobName = jenkWorkflowRequest.name();
        String jobGroup = jenkWorkflowRequest.group();
        String jobBranch = jenkWorkflowRequest.branch();
        Integer buildId = jenkWorkflowRequest.build();

        JenkinsClient client = JenkinsClient.builder()
                .endPoint(jenkinsWorkflowConfig.host().toURI().toString())
                .build();

        JobsApi jobsApi = client.api().jobsApi();
        JobList jobList = Optional.ofNullable(jobsApi.jobList(jobGroup + "/" + jobName))
                .orElseThrow(() -> new JenkinsAPINotFound("Jenkins Job not found (group: %s name: %s)".formatted(jobGroup, jobName)));
        JobInfo jobInfo = Optional.ofNullable(jobsApi.jobInfo(jobGroup + "/" + jobName, jobBranch))
                .orElseThrow(() -> new JenkinsAPINotFound("Jenkins Job Info not found (JobGroup: %s JobName: %s JobBranch: %s)".formatted(jobGroup, jobName, jobBranch)));

        String workflowURL = buildWorkflowURL(jobList, jobBranch, buildId);
        URL buildInfoURL = URLConverter.string2url(workflowURL + "/api/json?depth=1").orElseThrow();
        log.info("buildinfo url: {} ", buildInfoURL);

        BuildInfo build = jobsApi.buildInfo(jobGroup + "/" + jobName, jobBranch, buildId);
        log.info("[*] build: {}", build);


        Workflow workflow = jobsApi.runHistory(jobGroup + "/" + jobName, jobBranch)
                .stream()
                .filter(d -> d.name().equals("#" + buildId))
                .findFirst()
                .orElseThrow(() -> new JenkinsAPINotFound("Jenkins Workflow not found (JobGroup: %s JobName: %s JobBranch: %s)".formatted(jobGroup, jobName, jobBranch))); // .get() para que salga la excepcion?

        log.info("workflow: {}", workflow);

        // Create trace

        Id id = new Id(jobGroup, jobName, jobBranch, buildId);
        JenkinsBuildInfoCrawler jenkinsBuildInfoCrawler = new JenkinsBuildInfoCrawler(buildInfoURL);
        JenkinsStagesFetcher jenkinsStagesFetcher = new JenkinsStagesFetcher();
        jenkinsStagesFetcher.fetch(id, workflowURL, workflow.stages());
        Trace trace = new Trace(
                Status.valueOf(build.result()),
                workflow.startTimeMillis(),
                workflow.startTimeMillis() + workflow.durationTimeMillis(),
                URLConverter.string2url(build.url()).orElseThrow(),
                jenkinsStagesFetcher.getStages(),
                fetchChangesets(id, jenkinsBuildInfoCrawler, build.changeSets())
        );
        return new JenkinsWorkflow(id,
                applicationId,
                jobInfo.displayName(),
                build.fullDisplayName(),
                URLConverter.string2url(jobInfo.url()).orElseThrow(),
                githubInfo(buildInfoURL), // This is probably wrong and misscalculated (always null)
                trace,
                jenkinsStagesFetcher.getDockerImage());
    }

    private List<Changeset> fetchChangesets(Id jenkinsId,
                                            JenkinsBuildInfoCrawler jenkinsBuildInfoCrawler,
                                            List<ChangeSetList> changeSetLists) throws MalformedURLException, URISyntaxException{
        List<Changeset> changesets = new ArrayList<>();
        // Conversion changesets
        for (ChangeSetList changeSet : changeSetLists) {
            for (ChangeSet changeSetItem : changeSet.items()) {
                log.info("[*] Commit {}", changeSetItem.commitId());
                String commit = changeSetItem.commitId();
                String repoURL = jenkinsBuildInfoCrawler.getCommitURL(commit);
                String[] splits = repoURL.split("/");
                String group = splits[0];
                String repo = splits[1];
                if (repo.contains(".")) {
                    repo = repo.split("\\.")[0];
                }
                Changeset changeset = new Changeset(
                        jenkinsId,
                        group,
                        repo,
                        changeSetItem.commitId(),
                        changeSetItem.comment(),
                        changeSetItem.author().fullName(),
                        URLConverter.string2url(changeSetItem.author().absoluteUrl()).orElseThrow(),
                        changeSetItem.authorEmail(),
                        changeSetItem.affectedPaths(),
                        changeSetItem.timestamp()
                );
                changesets.add(changeset);
            }
        }
        return changesets;
    }

    private URL githubInfo(URL url) throws IOException, URISyntaxException {
        if (url == null) return null;
        try (InputStream stream = url.openStream()) {
            JsonNode json = MAPPER.readTree(stream);
            JsonNode actions = json.get("actions");
            for (JsonNode item : actions) {
                if (item.isEmpty()) continue;
                if ("jenkins.scm.api.metadata.ObjectMetadataAction".equals(item.get("_class").asText())) {
                    String objectUrl = item.get("objectUrl").asText();
                    if (!ObjectUtils.isEmpty(objectUrl)) {
                        return URLConverter.string2url(objectUrl).orElseThrow();
                    }
                }
            }
        }
        return null;

    }

    private static String buildWorkflowURL(JobList job, String jobBranch, Integer buildId) {
        return String.format("%sjob/%s/%d", job.url(), jobBranch, buildId);
    }
}
