package eu.ai4eosc.provenance.api.fetcher.nomad;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.NomadDeploymentId;
import eu.ai4eosc.provenance.api.fetcher.nomad.config.NomadConfig;
import eu.ai4eosc.provenance.api.fetcher.nomad.dfo.FetchedNomadDeployment;
import eu.ai4eosc.provenance.api.fetcher.nomad.dfo.NomadDeployment;
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
import java.util.Optional;

import eu.ai4eosc.provenance.api.fetcher.ProvenanceProvider;

@Service
public class NomadJobFetcher implements ProvenanceProvider {
    static final Logger log = LoggerFactory.getLogger(NomadJobFetcher.class);
    private static final String API_ENDPOINT = "/v1/deployments";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private NomadConfig nomadConfig;

    @Override
    public Optional<NomadDeployment> fetch(Record input, String applicationId
    ) throws URISyntaxException, IOException, InterruptedException {
        String deploymentId = ((NomadDeploymentId) input).deploymentId();
        NomadDeployment nomadDeployment = new NomadDeployment();
        log.info("Nomad fetching -> {} ...", deploymentId);
        String fetchNomadJobMetadataResponse =
                fetchNomadJob(nomadConfig.host(),
                        nomadConfig.credentials().vo(),
                        nomadConfig.fullInfo(),
                        deploymentId);
        var fetchedNomadDeployment = MAPPER.readValue(fetchNomadJobMetadataResponse, FetchedNomadDeployment.class);
        if (fetchedNomadDeployment.jobId() == null) {
            return Optional.empty();
        }
        nomadDeployment.setFetchedNomadDeployment(fetchedNomadDeployment);
        nomadDeployment.setApplicationId(applicationId);
        nomadDeployment.setOrigin("Nomad");
        return Optional.of(nomadDeployment);
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
            log.info("request -> {}", request.uri());
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.body();
        }

    }

    private URI uri(URL host, String endpoint) throws URISyntaxException {
        return URI.create(host.toURI().toString() + API_ENDPOINT + endpoint);
    }
}
