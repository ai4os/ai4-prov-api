package eu.ai4eosc.provenance.api.controllers;

import static eu.ai4eosc.provenance.api.models.Type.MappingFormat;

import java.io.ByteArrayInputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ai4eosc.provenance.api.controllers.schemas.MetadataOrigin;
import eu.ai4eosc.provenance.api.fetcher.fetchers.NomadJobFetcher;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.details.Details;
import eu.ai4eosc.provenance.api.fetcher.fetchers.DetailsProjectFetcher;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins.JenkinsWorkflow;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.mlflow.Experiment;
import eu.ai4eosc.provenance.api.fetcher.fetchers.JenkinsPublicationFetcher;
import eu.ai4eosc.provenance.api.fetcher.fetchers.MLFlowExperimentFetcher;
import eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.nomad.NomadJob;
import eu.ai4eosc.provenance.api.services.MetadataService;
import eu.ai4eosc.provenance.api.services.RDFGraphService;
import eu.ai4eosc.provenance.api.services.rdfgraph.IRINode;
import eu.ai4eosc.provenance.api.services.rdfgraph.ProvGraphBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProvenanceController {

    final static Logger logger = LoggerFactory.getLogger(ProvenanceController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    NomadJobFetcher nomadJF;
    @Autowired
    MLFlowExperimentFetcher mlflowEF;
    @Autowired
    JenkinsPublicationFetcher jenkinsPF;
    @Autowired
    DetailsProjectFetcher detailsPF;
    @Autowired
    MetadataService metadataService;
    @Autowired
    RDFGraphService rdfGraphService;

    @PostMapping("/meta-data")
    ResponseEntity<?> fetchJenkinsMlflowMetadata(
            @RequestBody MetadataOrigin metadata
    ) throws Exception {
        String metadataType = "details_jenkins_mlflow";
        ObjectNode composedJson = MAPPER.createObjectNode();
        // Fetch nomad job JSON metadata (optional)
        if (metadata.nomadJob() != null) {
            NomadJob nomadMetadata = nomadJF.fetch(metadata.nomadJob(), metadata.applicationId());
            composedJson.set("nomad-metadata", MAPPER.valueToTree(nomadMetadata));
            metadataType = "details_jenkins_mlflow_nomad";
        }

        // Fetch mlflow JSON metadata
        Experiment mlflowMetadata = mlflowEF.fetch(metadata.mlflowExperiment(), metadata.applicationId());
        composedJson.set("mlflow-metadata", MAPPER.valueToTree(mlflowMetadata));

        // Fetch jenkins JSON metadata
        JenkinsWorkflow jenkinsWorkflowMetadata = jenkinsPF.fetch(metadata.jenkinsWorkflow(), metadata.applicationId());
        composedJson.set("jenkins-metadata", MAPPER.valueToTree(jenkinsWorkflowMetadata));

        // Fetch application details
        Details detailsMetadata = detailsPF.fetch(metadata.applicationId());
        composedJson.set("details-metadata", MAPPER.valueToTree(detailsMetadata));


        var count = metadataService.upsertInstance(metadataType,
                metadata.applicationId(),
                composedJson);
        return ResponseEntity.ok("metadata saved");
    }

    @GetMapping("/provenance")
    ResponseEntity<?> provenance(
            @RequestParam("applicationId") String applicationId,
            @RequestParam(name = "type", required = false, defaultValue = "details_jenkins_mlflow") String typeId
    ) throws Exception {
        var outputFormat = MappingFormat.JSON;
        try (var is = metadataService.rmlMapping(typeId, applicationId, outputFormat).orElseThrow();) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(outputFormat.getContentType()))
                    .body(new InputStreamResource(is));
        }
    }


    @GetMapping("/prov-graph")
    ResponseEntity<?> provenanceGraph(@RequestParam("applicationId") String applicationId,
                                      @RequestParam(name = "type", required = false, defaultValue = "details_jenkins_mlflow") String typeId) throws Exception {
        try (var is = metadataService.rmlMapping(typeId, applicationId).orElseThrow();) {
            ProvGraphBuilder provGraphBuilder = rdfGraphService.buildGraph(is).orElseThrow();
            //  RDF graph passed as a model attribute to show it in frontend
            // returns the index template
            return new ResponseEntity<Map<IRI, IRINode>>(provGraphBuilder.getRdfGraph(), HttpStatus.OK);
        }
    }

    @GetMapping("/details-metadata")
    ResponseEntity<?> obtainApplicationDetails(@RequestParam("applicationId") String applicationId,
                                                @RequestParam(name = "type", required = false, defaultValue = "deeph") String type) throws Exception {
        Details detailsMetadata = detailsPF.fetch(applicationId);
        JsonNode detailsJson;
        if (type.equals("fair4ml")) {
            detailsJson = MAPPER.valueToTree(detailsMetadata);
            ObjectNode composedJson = MAPPER.createObjectNode();
            composedJson.set("details-metadata", detailsJson);
            ByteArrayInputStream fair4ml = metadataService.directRmlMapping("details_fair4ml", composedJson).orElseThrow();
            return ResponseEntity.ok().contentType(MediaType.valueOf("application/json"))
                    .body(new InputStreamResource((fair4ml)));
        }

        // Some kind of processing to change format
        detailsJson = MAPPER.valueToTree(detailsMetadata);
        return ResponseEntity.ok().contentType(MediaType.valueOf("application/json")).body(detailsJson);
    }
}
