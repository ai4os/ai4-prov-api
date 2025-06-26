package eu.ai4eosc.provenance.api.fetcher.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JenkinsBuildInfoCrawler {
    static final Logger log = LoggerFactory.getLogger(JenkinsBuildInfoCrawler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, String> commitRepo;

    public JenkinsBuildInfoCrawler(URL jenkinsBuildInfoURL) throws IOException {
        this.commitRepo = new HashMap<>();
        JsonNode buildInfoActions;
        try (InputStream is = jenkinsBuildInfoURL.openStream()) {
            buildInfoActions = MAPPER.readTree(is).get("actions");
        }
        for (JsonNode node : buildInfoActions) {
            JsonNode clas = node.get("_class");
            JsonNode lastBuiltRevision = node.get("lastBuiltRevision");
            JsonNode remoteUrls = node.get("remoteUrls");
            if (clas == null || lastBuiltRevision == null) continue;
            log.info("class: {}", clas);

            String commit = lastBuiltRevision.get("SHA1").asText();
            log.info("lastBuiltRev: {}", commit);

            String repoUrl = this.obtainRepoFromURL(remoteUrls.get(0).asText());
            log.info("remoteUrls: {}", repoUrl);

            commitRepo.put(commit, repoUrl);
        }
    }

    public String getCommitURL(String commit) {
        return commitRepo.getOrDefault(commit, "ai4os/ai4os-hub-qa");
    }

    private String obtainRepoFromURL(String url) {
        // we assume that the repos are always in github
        // because we are writing it in the rml mapping (no other option for now)
        int preRepoSlashIndex = 0;
        boolean foundAnySlashes = false;
        for (int i = url.length() - 1; i >= 0; i--) {
            if (url.charAt(i) == '/') {
                if (foundAnySlashes) {
                    preRepoSlashIndex = i;
                    break;
                }
                foundAnySlashes = true;
            }
        }
        return url.substring(preRepoSlashIndex + 1);
    }
}
