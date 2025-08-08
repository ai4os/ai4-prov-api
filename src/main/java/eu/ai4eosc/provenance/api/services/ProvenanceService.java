package eu.ai4eosc.provenance.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.Credentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.MLFlowCredentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.*;
import eu.ai4eosc.provenance.api.controllers.schemas.output.ProvenanceDate;
import eu.ai4eosc.provenance.api.fetcher.details.DetailsProjectFetcher;
import eu.ai4eosc.provenance.api.fetcher.details.dfo.Details;
import eu.ai4eosc.provenance.api.fetcher.details.dfo.FetchedDetails;
import eu.ai4eosc.provenance.api.fetcher.details.dfo.Provenance;
import eu.ai4eosc.provenance.api.fetcher.jenkins.JenkinsPublicationFetcher;
import eu.ai4eosc.provenance.api.fetcher.jenkins.dfo.JenkinsWorkflow;
import eu.ai4eosc.provenance.api.fetcher.mlflow.MLFlowFetcher;
import eu.ai4eosc.provenance.api.fetcher.mlflow.dfo.Experiment;
import eu.ai4eosc.provenance.api.fetcher.nomad.NomadJobFetcher;
import eu.ai4eosc.provenance.api.fetcher.nomad.dfo.NomadDeployment;
import eu.ai4eosc.provenance.api.services.graph.IRINode;
import eu.ai4eosc.provenance.api.services.graph.GraphBuilder;
import eu.ai4eosc.provenance.api.tables.InstanceTable;
import eu.ai4eosc.provenance.api.tables.TypeTable;
import org.eclipse.rdf4j.model.IRI;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
public class ProvenanceService {
    final static Logger log = LoggerFactory.getLogger(ProvenanceService.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int JENKINS_WAIT_IN_SECONDS = 10;

    @Autowired
    private DSLContext dsl;

    @Autowired
    DetailsProjectFetcher detailsPF;
    @Autowired
    NomadJobFetcher nomadJF;
    @Autowired
    MLFlowFetcher mlflowEF;
    @Autowired
    JenkinsPublicationFetcher jenkinsPF;

    @Autowired
    MetadataService metadataService;

    @Autowired
    GraphService rdfGraphService;

    public void createMetadataInstance(AllSourcesIDs sources, Credentials credentials) throws IOException, InterruptedException, URISyntaxException, MetadataService.TypeRMLNotFoundException {
        ObjectNode metadataJSON = MAPPER.createObjectNode();
        fetchApplicationDetails(metadataJSON, sources.applicationId());
        fetchNomadDeployment(metadataJSON, sources.nomadDeployment(), sources.applicationId());
        fetchMLFlowExperiment(metadataJSON, credentials.mlflow(), sources.mlflowRun(), sources.applicationId());
        fetchJenkinsWorkflow(metadataJSON, sources.jenkinsWorkflow(), sources.applicationId());
        upsertMetadataInstance(metadataJSON, sources.applicationId());
    }

    /**
     * Creates a new metadata JSON instance in DB
     *
     * @param sources, sources where the metadata will be looked for
     * @throws Exception
     */
    public void createMetadataInstance(SourcesIDs sources, Credentials credentials
    ) throws IOException, InterruptedException, URISyntaxException {
        ObjectNode metadataJSON = MAPPER.createObjectNode();
        Details details = fetchApplicationDetails(metadataJSON, sources.applicationId());

        NomadDeploymentId nomadDeploymentId = null;
        MLFlowRunId mlFlowExperimentId = null;

        Provenance prov = details.getFetchedDetails().provenance();
        if (prov != null) {
            nomadDeploymentId = new NomadDeploymentId(prov.nomadDepId());
            mlFlowExperimentId = new MLFlowRunId(prov.mlflowRun());
        }
        if (nomadDeploymentId != null && nomadDeploymentId.deploymentId() != null) {
            fetchNomadDeployment(metadataJSON, nomadDeploymentId, sources.applicationId());
        }
        if (mlFlowExperimentId != null && mlFlowExperimentId.runId() != null) {
            // Fetch mlflow JSON metadata (optional)
            fetchMLFlowExperiment(metadataJSON, credentials.mlflow(), mlFlowExperimentId, sources.applicationId());
        }

        CompletableFuture.runAsync(() -> {
            try {
                boolean jenkinsMetadataFetchCompleted = false;
                while (!jenkinsMetadataFetchCompleted) {
                    Thread.sleep(((long) JENKINS_WAIT_IN_SECONDS) * 1000);
                    try {
                        fetchJenkinsWorkflow(metadataJSON, sources.jenkinsWorkflow(), sources.applicationId());
                        upsertMetadataInstance(metadataJSON, sources.applicationId());
                        jenkinsMetadataFetchCompleted = true;
                        log.info("try to fetch jenkins metadata: Jenkins Fetch Success!");
                    } catch (Exception e) {
                        log.info("try to fetch jenkins metadata: Jenkins Fetch Failure!");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("During jenkins pipeline metadata waiting...", e);
            } catch (Exception e) {
                log.error("During jenkins pipeline metadata waiting...", e);
            }
        }).orTimeout(3, TimeUnit.MINUTES);
    }

    private void upsertMetadataInstance(ObjectNode metadataJSON, String applicationId) throws MetadataService.TypeRMLNotFoundException {
        String rmlMatch = FindProvenanceRMLMatchFromSources(metadataJSON.fieldNames());
        log.info("RML MATCH ! -> {}", rmlMatch);
        metadataService.upsertInstance(rmlMatch, applicationId, metadataJSON);
    }

    private Details fetchApplicationDetails(ObjectNode jsonObject, String applicationId) throws IOException, InterruptedException {
        // Fetch application details
        Details detailsMetadata = detailsPF.fetch(applicationId);
        jsonObject.set("details", MAPPER.valueToTree(detailsMetadata));
        return detailsMetadata;
    }

    private JenkinsWorkflow fetchJenkinsWorkflow(ObjectNode jsonObject, JenkinsWorkflowId jenkinsWorkflow, String applicationId
    ) throws IOException, URISyntaxException {
        // Fetch jenkins JSON metadata
        JenkinsWorkflow fetchedJenkinsWorkflow = jenkinsPF.fetch(jenkinsWorkflow, applicationId);
        log.info("jenkins workflow fetched: {}", fetchedJenkinsWorkflow);
        jsonObject.set("jenkins", MAPPER.valueToTree(fetchedJenkinsWorkflow));
        return fetchedJenkinsWorkflow;
    }

    private Experiment fetchMLFlowExperiment(ObjectNode jsonObject,
                                       MLFlowCredentials mlFlowCredentials,
                                       MLFlowRunId mlFlowRunId,
                                       String applicationId) throws URISyntaxException, IOException, InterruptedException {
        Optional<Experiment> fetchedMLFlowExperiment = mlflowEF.fetch(mlFlowCredentials, mlFlowRunId, applicationId);
        log.info("mlflow experiment fetched: {}", fetchedMLFlowExperiment);
        fetchedMLFlowExperiment.ifPresent(experiment -> jsonObject.set("mlflow", MAPPER.valueToTree(experiment)));
        return fetchedMLFlowExperiment.orElse(null);
    }

    private NomadDeployment fetchNomadDeployment(ObjectNode jsonObject, NomadDeploymentId nomadDeploymentId, String applicationId
    ) throws URISyntaxException, IOException, InterruptedException {
        // Fetch nomad job JSON metadata (optional)
        Optional<NomadDeployment> nomadDeployment = nomadJF.fetch(nomadDeploymentId, applicationId);
        log.info("nomad deployment fetched: {}", nomadDeployment);
        nomadDeployment.ifPresent(deployment -> jsonObject.set("nomad", MAPPER.valueToTree(deployment)));
        return nomadDeployment.orElse(null);
    }

    String FindProvenanceRMLMatchFromSources(Iterator<String> sources) {
        Iterable<String> iterable = () -> sources;
        List<String> sourcesList = new ArrayList<>(StreamSupport.stream(iterable.spliterator(), false).toList());
        return TypeTable.getProvenanceIdFromSourceList(sourcesList);
    }

    public ProvenanceDate getProvenanceDate(String applicationId) {
        LocalDateTime creationDate = InstanceTable.findMoreRecent(dsl, applicationId).orElseThrow().creationDate();
        return new ProvenanceDate(creationDate);
    }

    public ProvenanceDate getProvenanceDate(String applicationId, String typeId) {
        LocalDateTime creationDate = InstanceTable.find(dsl, typeId, applicationId).orElseThrow().creationDate();
        return new ProvenanceDate(creationDate);
    }

    public Map<IRI, IRINode> generateProvenanceGraph(String applicationId) throws IOException {
        String typeId = InstanceTable.findMoreRecent(dsl, applicationId).orElseThrow().type();
        return generateProvenanceGraph(applicationId, typeId);
    }

    public Map<IRI, IRINode> generateProvenanceGraph(String applicationId, String typeId) throws IOException {
        log.info("generating graph for: application {} + type {}", applicationId, typeId);
        var rdf = metadataService.rmlMapping(typeId, applicationId);
        GraphBuilder provGraphBuilder = rdfGraphService.buildGraph(rdf);
        //  RDF graph passed as a model attribute to show it in frontend
        // returns the index template
        return provGraphBuilder.getRdfGraph();

    }

    public JsonNode getProvenanceRDF(String applicationId) throws IOException {
        String typeId = InstanceTable.findMoreRecent(dsl, applicationId).orElseThrow().type();
        return getProvenanceRDF(applicationId, typeId);
    }

    public JsonNode getProvenanceRDF(String applicationId, String typeId) throws IOException {
        return MAPPER.readTree(metadataService.rmlMapping(typeId, applicationId));
    }

    public JsonNode getApplicationDetails(String applicationId, String format) throws IOException, InterruptedException {
        FetchedDetails details = detailsPF.fetch(applicationId).getFetchedDetails();
        JsonNode detailsJson;
        if (format.equals("fair4ml")) {
            detailsJson = MAPPER.valueToTree(details);
            ObjectNode composedJson = MAPPER.createObjectNode();
            composedJson.set("details-metadata", detailsJson);
            String rdf = metadataService.directRmlMapping("details_to_fair4ml", composedJson);
            // Returns fair4ml rdf
            return MAPPER.readTree(rdf);

        }
        // Returns the raw details json as come from PAPI
        detailsJson = MAPPER.valueToTree(details);
        return detailsJson;
    }

}
