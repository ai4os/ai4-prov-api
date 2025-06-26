package eu.ai4eosc.provenance.api.fetcher.mlflow;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.ICredentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.MLFlowCredentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.MLFlowRunId;
import eu.ai4eosc.provenance.api.fetcher.ProvenanceProviderWithCredentials;
import eu.ai4eosc.provenance.api.fetcher.mlflow.config.MLFlowConfig;
import eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.FetchedExperiment;
import eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.run.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.Experiment;
import eu.ai4eosc.provenance.api.fetcher.ProvenanceProvider;

@Service
public class MLFlowFetcher implements ProvenanceProviderWithCredentials {
    static final Logger log = LoggerFactory.getLogger(MLFlowFetcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String API_ENDPOINT = "/api/2.0/mlflow/";
    private static final String EXPERIMENT_ENDPOINT = "experiments/get?experiment_id=";
    private static final String RUNS_ENDPOINT = "runs/get?run_id=";

    @Autowired
    private MLFlowConfig mlflowSettings;

    @Override
    public Optional<Experiment> fetch(ICredentials credentials, Record input, String applicationId
    ) throws URISyntaxException, IOException, InterruptedException {
        MLFlowCredentials mlFlowCredentials = (MLFlowCredentials) credentials;
        URL ai4eoscHost = mlflowSettings.ai4eosc().host();
        URL imagineHost = mlflowSettings.imagine().host();
        String runId = ((MLFlowRunId) input).runId();
        log.info("mlflow run id: {}", runId);
        Experiment experiment = new Experiment();
        experiment.setOrigin("MLflow");
        experiment.setApplicationId(applicationId);

        FetchedRun fetchedRun = fetchRunFromOrigin(ai4eoscHost, mlFlowCredentials, runId);
        log.info("first fetch checked !!!");
        FetchedExperiment fetchedExperiment;
        // Try to fetch the mlflow run first from AI4EOSC and if it does not work, fetch from Imagine
        if (fetchedRun == null) {
            fetchedRun = fetchRunFromOrigin(mlflowSettings.imagine().host(), mlFlowCredentials, runId);
            if (fetchedRun == null) return Optional.empty();
            fetchedExperiment = fetchExperimentFromOrigin(imagineHost, mlFlowCredentials, fetchedRun.info().experimentId());
        } else {
            fetchedExperiment = fetchExperimentFromOrigin(ai4eoscHost, mlFlowCredentials, fetchedRun.info().experimentId());
        }

        // Fill metrics, params and tags with runId for later RML mapping
        fillAllDataPropertiesWithId(fetchedRun);
        // Create FetchedRunWrapper to link it with applicationId for later RML mapping
        FetchedRunWrapper frw = new FetchedRunWrapper(applicationId, fetchedRun);
        // Set the final attributes of the experiment
        experiment.setRun(frw);
        experiment.setFetchedExperiment(fetchedExperiment);
        return Optional.of(experiment);
    }

    void fillAllDataPropertiesWithId(FetchedRun fetchedRun) {
        String runId = fetchedRun.info().runId();
        // Fill metrics
        var metrics = fetchedRun.data().metrics();
        for (Metric metric : metrics) {
            metric.setRunId(runId);
        }

        // Fill params
        var params = fetchedRun.data().params();
        for (KeyValue param : params) {
            param.setRunId(runId);
        }

        // Fill tags
        var tags = fetchedRun.data().tags();
        for (KeyValue tag : tags) {
            tag.setRunId(runId);
        }
    }

    private FetchedRun fetchRunFromOrigin(URL mlflowHost, MLFlowCredentials credentials, String runId
    ) throws URISyntaxException, IOException, InterruptedException {
        try (HttpClient httpClient = client(credentials);) {
            String response = fetchRun(httpClient, mlflowHost, runId);
            JsonNode rootNode = MAPPER.readTree(response);
            return MAPPER.treeToValue(rootNode.get("run"), FetchedRun.class);
        }

    }

    private FetchedExperiment fetchExperimentFromOrigin(URL mlflowHost, MLFlowCredentials credentials,
                                                        String experimentId) throws URISyntaxException, IOException, InterruptedException {
        try (HttpClient httpClient = client(credentials);) {
            String response = fetchExperiment(httpClient, mlflowHost, experimentId);
            JsonNode rootNode = MAPPER.readTree(response);
            return MAPPER.treeToValue(rootNode.get("experiment"), FetchedExperiment.class);
        }
    }

    private URI uri(URL host, String endpoint) throws URISyntaxException {
        return URI.create(host.toURI().toString() + API_ENDPOINT + endpoint);
    }

    private String fetchRun(HttpClient client, URL host,
                            String runId) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri(host, RUNS_ENDPOINT + runId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String fetchExperiment(HttpClient client, URL host,
                                   String experimentId) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri(host, EXPERIMENT_ENDPOINT + experimentId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    private HttpClient client(MLFlowCredentials mlFlowCredentials) {
        return client(mlFlowCredentials.username(), mlFlowCredentials.password());
    }

    private HttpClient client(String username, String password) {
        return HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .followRedirects(Redirect.ALWAYS)
                .build();
    }
}
