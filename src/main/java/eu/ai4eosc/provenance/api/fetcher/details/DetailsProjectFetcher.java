package eu.ai4eosc.provenance.api.fetcher.details;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.fetcher.details.dfo.Details;
import eu.ai4eosc.provenance.api.fetcher.details.dfo.FetchedDetails;
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
    static final Logger log = LoggerFactory.getLogger(DetailsProjectFetcher.class);
    private static final String METADATA_PAPI_ENDPOINT = "https://api.cloud.ai4eosc.eu/v1/catalog/modules/project/metadata";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Details fetch(String applicationId) throws InterruptedException, IOException {
        String rawDetails = fetchDetails(applicationId);
        Details details = new Details();
        details.setOrigin("AI4EOSC");
        log.info("metadata details fetched!");
        FetchedDetails fetchedDetails = MAPPER.readValue(rawDetails, FetchedDetails.class);
        details.setFetchedDetails(fetchedDetails);
        return details;
    }

    private String fetchDetails(String applicationId) throws IOException, InterruptedException {
        try(HttpClient client = HttpClient.newBuilder().build();) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(METADATA_PAPI_ENDPOINT.replace("project", applicationId)))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }
}
