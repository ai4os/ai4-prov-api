package eu.ai4eosc.provenance.api.fetcher.jenkins;

import com.cdancy.jenkins.rest.domain.job.Stage;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.Id;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.JenkinsWorkflowStage;
import eu.ai4eosc.provenance.api.utils.URLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JenkinsStagesFetcher {
    static final Logger log = LoggerFactory.getLogger(JenkinsStagesFetcher.class);


    private static final String WORKFLOW_APP_DOCKER_DELIVERY_STEP_NAME = "AI4OS Hub Docker delivery to registry";
    private static final Pattern PATTERN_DOCKER_DIGEST = Pattern.compile("(gpu: digest: |latest: digest: )(sha256:[a-f0-9]{64})( size: \\d+)");


    List<JenkinsWorkflowStage> stages;
    String dockerImage;

    public JenkinsStagesFetcher() {
    }

    public List<JenkinsWorkflowStage> getStages() {return this.stages;}
    public String getDockerImage() {return this.dockerImage;}

    public void fetch(Id jenkinsId, String workflowURL, List<Stage> jenkinsStages
    ) throws IOException, URISyntaxException{
        List<JenkinsWorkflowStage> stages = new ArrayList<>();
        for (Stage st : jenkinsStages) {
            Map<String, Object> props = new HashMap<>();
            if (WORKFLOW_APP_DOCKER_DELIVERY_STEP_NAME.equalsIgnoreCase(st.name())) {
                Optional<String> dockerImgHash = findDockerImg(workflowURL, st.id());
                dockerImgHash.ifPresent(dockerImg -> {
                    props.put("appDockerDeliveryHash", dockerImg);
                    this.dockerImage = dockerImg;
                });
            }
            JenkinsWorkflowStage stage = new JenkinsWorkflowStage(
                    jenkinsId,
                    st.id(),
                    st.name(),
                    st.startTimeMillis(),
                    st.startTimeMillis() + st.durationMillis(),
                    st.durationMillis(),
                    props);
            stages.add(stage);
        }
        this.stages = stages;
    }

    private Optional<String> findDockerImg(String workflowURL, String stageId
    ) throws IOException, URISyntaxException {
        URL logUrl = URLConverter.string2url(workflowURL + "/pipeline-console/log?nodeId=" + stageId).orElseThrow();
        log.info("trying to find docker image in logURL -> {}", logUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logUrl.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!ObjectUtils.isEmpty(line)) {
                    Matcher matcher = PATTERN_DOCKER_DIGEST.matcher(line);
                    if (matcher.matches() && matcher.groupCount() == 3) {
                        log.info("found docker image delivery hash!! ");
                        return Optional.of(matcher.group(2));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
