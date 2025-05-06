package eu.ai4eosc.provenance.api.fetcher.fetchers;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import eu.ai4eosc.provenance.api.entities.MLFlowExperiment;
import eu.ai4eosc.provenance.api.fetcher.fetchers.config.MLFlowConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.mlflow.Experiment;


@Service
// @ConfigurationPropertiesScan
public class MLFlowExperimentFetcher implements ProvenanceProvider {
    final static Logger logger = LoggerFactory.getLogger(MLFlowExperimentFetcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String API_ENDPOINT = "/api/2.0/mlflow/";
    private static final String EXPERIMENT_ENDPOINT = "experiments/get-by-name?experiment_name=";
    private static final String RUNS_ENDPOINT = "runs/search";

    @Autowired
    private MLFlowConfig mlflowSettings;
    @Data
    @NoArgsConstructor
    static class ExperimentWrapper {
        Experiment experiment;
    };

    @Override
    public Experiment fetch(Record input, String applicationId) throws Exception {

        String experimentId = ((MLFlowExperiment) input).experimentId();
        logger.info("mlflowSettings: " + MAPPER.writeValueAsString(mlflowSettings.host()));
        HttpClient httpClient = client(mlflowSettings);

        Experiment experiment;
        try {
            String fetchExperimentResponse = fetchExperiment(httpClient, mlflowSettings.host(), experimentId);
            experiment = MAPPER.readValue(fetchExperimentResponse, ExperimentWrapper.class).getExperiment();
        } catch (Exception e) {
            throw new Exception("Error fetching experiment {}", e);
        }


        String runs = fetchRuns(httpClient, mlflowSettings.host(), experiment.getId());

        experiment.setApplicationId(applicationId);
        experiment.setOrigin("MLflow");
        return experiment;
    }

    private URI uri(URL host, String endpoint) throws URISyntaxException {
        return URI.create(host.toURI().toString() + API_ENDPOINT + endpoint);
    }

    private String fetchExperiment(HttpClient client, URL host, String experimentId) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri(host, EXPERIMENT_ENDPOINT + experimentId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String fetchRuns(HttpClient client, URL host, String experimentId) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri(host, RUNS_ENDPOINT))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"experiment_ids\":[\"" + experimentId + "\"]}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private HttpClient client(MLFlowConfig settings) {
        return client(settings.credentials().username(), settings.credentials().password());
    }

    private HttpClient client(String username, String password) {
        return HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .followRedirects(Redirect.ALWAYS)
                .build();
    }
}
