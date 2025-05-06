package eu.ai4eosc.provenance.api.fetcher.fetchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cdancy.jenkins.rest.domain.job.*;
import com.cdancy.jenkins.rest.features.JobsApi;
import eu.ai4eosc.provenance.api.entities.JenkinsWorkflowRequest;
import eu.ai4eosc.provenance.api.fetcher.fetchers.config.JenkinsWorkflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.Status;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.JenkinsWorkflow;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.Changeset;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.Id;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.JenkinsWorkflowStage;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.Trace;
import io.micrometer.common.util.StringUtils;

@Service
public class JenkinsPublicationFetcher implements ProvenanceProvider {
    final static Logger logger = LoggerFactory.getLogger(JenkinsPublicationFetcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String WORKFLOW_APP_DOCKER_DELIVERY_STEP_NAME = "AI4OS Hub Docker delivery to registry";
    private static final Pattern PATTERN_DOCKER_DIGEST = Pattern.compile("(gpu: digest: |latest: digest: )(sha256:[a-f0-9]{64})( size: \\d+)");

    @Autowired
    private JenkinsWorkflowConfig jenkinsWorkflowConfig;

    @Override
    public JenkinsWorkflow fetch(Record input, String applicationId) throws Exception {

        JenkinsWorkflowRequest jenkWorkflowRequest = (JenkinsWorkflowRequest) input;
        logger.info("request jenkins" + jenkWorkflowRequest);
        String jobName = jenkWorkflowRequest.jobName();
        String jobGroup = jenkWorkflowRequest.jobGroup();
        String jobBranch = jenkWorkflowRequest.jobBranch();
        Integer buildId = jenkWorkflowRequest.execution();
        JenkinsClient client = JenkinsClient.builder()
                .endPoint(jenkinsWorkflowConfig.host().toURI().toString())
                .build();
        JobsApi jobsApi = client.api().jobsApi();
        JobList jobList = Optional.ofNullable(jobsApi.jobList(jobGroup + "/" + jobName))
                .orElseThrow(() -> new Exception("Jenkins Job not found (jobGroup: %s jobName: %s)".formatted(jobGroup, jobName)));
        JobInfo jobInfo = Optional.ofNullable(jobsApi.jobInfo(jobGroup + "/" + jobName, jobBranch))
                .orElseThrow(() -> new Exception("Jenkins Job Info not found (JobGroup: %s JobName: %s JobBranch: %s)".formatted(jobGroup, jobName, jobBranch)));

        URL buildInfoURL = string2url(buildJobBuildURL(jobList, jobBranch, buildId) + "/api/json?depth=1");
        logger.info("buildinfo url: " + buildInfoURL);
        JenkinsBuildInfoCrawler jenkinsBuildInfoCrawler = new JenkinsBuildInfoCrawler(buildInfoURL);
        Workflow workflow = jobsApi.runHistory(jobGroup + "/" + jobName, jobBranch)
                .stream()
                .filter(d -> d.name().equals("#" + buildId))
                .findFirst()
                .orElseThrow(() -> new Exception("Jenkins Workflow not found (JobGroup: %s JobName: %s JobBranch: %s)".formatted(jobGroup, jobName, jobBranch))); // .get() para que salga la excepcion?

        logger.info("workflow: " + workflow);
        BuildInfo build = jobsApi.buildInfo(jobGroup + "/" + jobName, jobBranch, buildId);
        logger.info("[*] build: " + build + " buildUrl: " + build.url());
        Id id = new Id(jobGroup, jobName, jobBranch, buildId);
        // Stage Fetching
        List<JenkinsWorkflowStage> stages = new ArrayList<>();
        for (Stage st : workflow.stages()) {
            Map<String, Object> props = new HashMap<>();
            if (WORKFLOW_APP_DOCKER_DELIVERY_STEP_NAME.equalsIgnoreCase(st.name())) {
                // logger.info("stageId: " + st.id());
                // e.g. https://jenkins.services.ai4os.eu/job/{JOB_GROUP}/job/{JOB_NAME}/job/{JOB_BRANCH}/{BUILD_ID}/pipeline-console/log?nodeId={NODEID}
                URL logUrl = string2url(buildJobBuildURL(jobList, jobBranch, buildId) + "/pipeline-console/log?nodeId=" + st.id());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(logUrl.openStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (StringUtils.isNotEmpty(line)) {
                            Matcher matcher = PATTERN_DOCKER_DIGEST.matcher(line);
                            if (matcher.matches()) {
                                if (matcher.groupCount() != 3) break;
                                props.put("appDockerDeliveryHash", matcher.group(2));
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new Exception(String.format("Jenkins Pipeline log not found (JobGroup: %s JobName: %s JobBranch: %s BuildId: %s)", jobGroup, jobName, jobBranch, buildId));
                }
            }
            JenkinsWorkflowStage stage = new JenkinsWorkflowStage(
                    id,
                    st.id(),
                    st.name(),
                    st.startTimeMillis(),
                    st.startTimeMillis() + st.durationMillis(),
                    st.durationMillis(),
                    props);
            stages.add(stage);
        }

        List<Changeset> changesets = new ArrayList<>();
        // Conversion changesets
        for (ChangeSetList changeSet : build.changeSets()) {
            for (ChangeSet changeSetItem : changeSet.items()) {
                logger.info("[*] Commit {}", changeSetItem.commitId());
                String commit = changeSetItem.commitId();
                String repoURL = jenkinsBuildInfoCrawler.getCommitURL(commit);
                if (repoURL == null) repoURL = "ai4os/ai4os-hub-qa";
                String[] splits = repoURL.split("/");
                String group = splits[0];
                String repo = splits[1];
                if (repo.contains(".")) {
                    repo = repo.split("\\.")[0];
                }
                Changeset changeset = new Changeset(
                        id,
                        group,
                        repo,
                        changeSetItem.commitId(),
                        changeSetItem.comment(),
                        changeSetItem.author().fullName(),
                        string2url(changeSetItem.author().absoluteUrl()),
                        changeSetItem.authorEmail(),
                        changeSetItem.affectedPaths(),
                        changeSetItem.timestamp()
                );
                changesets.add(changeset);
            }
        }

        Trace trace = new Trace(
                Status.valueOf(build.result()),
                workflow.startTimeMillis(),
                workflow.startTimeMillis() + workflow.durationTimeMillis(),
                string2url(build.url()),
                stages,
                changesets
        );

        return new JenkinsWorkflow(id,
                applicationId,
                jobInfo.displayName(),
                build.fullDisplayName(),
                string2url(jobInfo.url()),
                githubInfo(buildInfoURL), // This is probably wrong and misscalculated (always null)
                trace);
    }

    private URL githubInfo(URL url) throws IOException, ParseException, URISyntaxException {
        // e.g. https://jenkins.services.ai4os.eu/job/{JOB_GROUP}/job/{JOB_NAME}/job/{JOB_BRANCH}/{BUILD_ID}/api/json?depth=1&pretty=true
        if (url == null) return null;
        // logger.info("githubInfo: " + url);
        try (InputStream stream = url.openStream()) {
            JsonNode json = MAPPER.readTree(stream);
            JsonNode actions = (JsonNode) json.get("actions");
            Iterator<JsonNode> it = actions.iterator();
            while (it.hasNext()) {
                JsonNode item = it.next();
                if (item.isEmpty()) continue;

                if ("jenkins.scm.api.metadata.ObjectMetadataAction".equals(item.get("_class").asText())) {
                    String objectUrl = item.get("objectUrl").asText();
                    if (!ObjectUtils.isEmpty(objectUrl)) {
                        // logger.info("objectUrl: " + objectUrl);
                        return string2url(objectUrl);
                    }
                }
            }
        }
        return null;

    }

    private static URL string2url(String url) throws MalformedURLException, URISyntaxException {
        if (!ObjectUtils.isEmpty(url)) {
            logger.info("url --> " + url);
            return new URI(url).toURL();
        }
        return null;
    }

    private static String buildJobBuildURL(JobList job, String jobBranch, Integer buildId) {
        return String.format("%sjob/%s/%d", job.url(), jobBranch, buildId);
    }

    private static JsonNode fetchAllRemoteRepo(URL jenkinsBuildInfoURL) throws IOException {
        try (InputStream is = jenkinsBuildInfoURL.openStream()) {
            return MAPPER.readTree(is);
        }
    }
}
