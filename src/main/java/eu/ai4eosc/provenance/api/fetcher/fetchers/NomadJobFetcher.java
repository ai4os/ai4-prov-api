package eu.ai4eosc.provenance.api.fetcher.fetchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.entities.NomadJobRequest;
import eu.ai4eosc.provenance.api.fetcher.fetchers.config.NomadConfig;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.nomad.NomadJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class NomadJobFetcher implements ProvenanceProvider {
    final static Logger logger = LoggerFactory.getLogger(NomadJobFetcher.class);
    private static final String API_ENDPOINT = "/v1/deployments";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private NomadConfig nomadConfig;

    @Override
    public NomadJob fetch(Record input, String applicationId) throws Exception {
        String deploymentId = ((NomadJobRequest) input).deploymentId();
        logger.info("(Start) Nomad fetching -> {}", deploymentId);
        NomadJob nomadJob;
        try {
            String fetchNomadJobMetadataResponse = fetchNomadJob(nomadConfig.host(),
                    nomadConfig.credentials().vo(),
                    nomadConfig.fullInfo(),
                    deploymentId);
            nomadJob = MAPPER.readValue(fetchNomadJobMetadataResponse, NomadJob.class);
        } catch (Exception e) {
            throw new Exception("Error fetching nomad job metadata {}", e);
        }
        nomadJob.setApplicationId(applicationId);
        nomadJob.setOrigin("Nomad");
        logger.info("(End) Nomad fetching -> {}", deploymentId);
        return nomadJob;
    }

    private String fetchNomadJob(URL host,
                                 String virtualOrg,
                                 Boolean fullInfo,
                                 String deploymentId) throws URISyntaxException, IOException, InterruptedException {
        try (
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();
        ) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri(host, "/modules/%s".formatted(deploymentId)
                            + "?vo=%s&full_info=%b".formatted(virtualOrg, fullInfo)))
                    .header("Authorization",
                            "Bearer %s".formatted(nomadConfig.credentials().token())).GET()
                    .build();
            logger.info("request -> " + request.uri());
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.body();
        }

    }
    private URI uri(URL host, String endpoint) throws URISyntaxException {
        return URI.create(host.toURI().toString() + API_ENDPOINT + endpoint);
    }
}
