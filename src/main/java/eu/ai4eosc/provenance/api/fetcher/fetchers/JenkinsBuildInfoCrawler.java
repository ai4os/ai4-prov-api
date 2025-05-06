package eu.ai4eosc.provenance.api.fetcher.fetchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JenkinsBuildInfoCrawler {
    final static Logger logger = LoggerFactory.getLogger(JenkinsBuildInfoCrawler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, String> commitRepo;

    public JenkinsBuildInfoCrawler(URL jenkinsBuildInfoURL) throws Exception {
        this.commitRepo = new HashMap<>();
        JsonNode buildInfoActions;
        try (InputStream is = jenkinsBuildInfoURL.openStream()) {
            buildInfoActions = MAPPER.readTree(is).get("actions");
        } catch (IOException e) {
            logger.error("Something went wrong parsing jenkins build info...");
            throw new Exception("Error in JenkinsBuildInfoCrawler constructor");
        }
        for (JsonNode node : buildInfoActions) {
            JsonNode clas = node.get("_class");
            JsonNode lastBuiltRevision = node.get("lastBuiltRevision");
            if (clas == null || lastBuiltRevision == null) continue;
            logger.info("class: " + clas.asText());
            String commit = lastBuiltRevision.get("SHA1").asText();
            logger.info("lastBuiltRev: " + commit);
            JsonNode remoteUrls = node.get("remoteUrls");
            String repoUrl = this.obtainRepoFromURL(remoteUrls.get(0).asText());
            logger.info("remoteUrls: " + repoUrl);
            commitRepo.put(commit, repoUrl);
        }
    }

    public String getCommitURL(String commit) {
        return commitRepo.getOrDefault(commit, null);
    }

    private String obtainRepoFromURL(String url) {
        // For now we assume that the repos are always in github
        // because we are writing in it in the rml mapping
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
