package eu.ai4eosc.provenance.api.fetcher.fetchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.details.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DetailsProjectFetcher implements DetailsProvider {
    final static Logger logger = LoggerFactory.getLogger(DetailsProjectFetcher.class);
    private static final String metadataPAPIAI4EOSC = "https://api.cloud.ai4eosc.eu/v1/catalog/tools/project/metadata";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Details fetch(String applicationId) throws Exception {
        try {
            String rawDetails = fetchDetails(applicationId);
            return MAPPER.readValue(rawDetails, Details.class);
        } catch (Exception e) {
            throw new Exception("Error fetching experiment {}", e);
        }
    }

    private String fetchDetails(String applicationId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metadataPAPIAI4EOSC.replace("project", applicationId)))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
