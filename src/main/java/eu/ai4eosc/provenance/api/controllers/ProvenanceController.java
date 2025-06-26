package eu.ai4eosc.provenance.api.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ai4eosc.provenance.api.controllers.schemas.input.FetchMetadataRequest;
import eu.ai4eosc.provenance.api.controllers.schemas.input.LegacyFetchMetadataRequest;
import eu.ai4eosc.provenance.api.controllers.schemas.input.credentials.Credentials;
import eu.ai4eosc.provenance.api.controllers.schemas.input.servicesids.SourcesIDs;
import eu.ai4eosc.provenance.api.controllers.schemas.output.ProvenanceDate;
import eu.ai4eosc.provenance.api.controllers.schemas.output.ProvenanceGraph;
import eu.ai4eosc.provenance.api.services.ProvenanceService;
import eu.ai4eosc.provenance.api.services.graph.IRINode;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProvenanceController {

    static final Logger log = LoggerFactory.getLogger(ProvenanceController.class);

    @Autowired
    ProvenanceService provenanceService;

    @PostMapping("/v0/meta-data")
    ResponseEntity<String> legacyFetchMetadata(
            @RequestBody LegacyFetchMetadataRequest request
            ) throws IOException, InterruptedException, URISyntaxException {
        provenanceService.createMetadataInstance(request.sources(), request.credentials());
        return ResponseEntity.ok("provenance fetched!");
    }

    @PostMapping("/meta-data")
    ResponseEntity<String> fetchMetadata(
            @RequestBody FetchMetadataRequest request
            ) throws IOException, InterruptedException, URISyntaxException {
        provenanceService.createMetadataInstance(request.sources(), request.credentials());
        return ResponseEntity.ok("fetching provenance...");
    }

    @GetMapping("/provenance")
    ResponseEntity<JsonNode> provenance(
            @RequestParam("applicationId") String applicationId,
            @RequestParam(name = "type", required = false) String typeId
    ) throws IOException {
        JsonNode rdf = (typeId == null) ?
                provenanceService.getProvenanceRDF(applicationId) :
                provenanceService.getProvenanceRDF(applicationId, typeId);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(rdf);
    }

    @GetMapping("/provenance-date")
    ResponseEntity<ProvenanceDate> provenanceDate(
            @RequestParam("applicationId") String applicationId,
            @RequestParam(name="type", required = false) String typeId
    ) {
        ProvenanceDate provDate = (typeId == null) ?
                provenanceService.getProvenanceDate(applicationId) :
                provenanceService.getProvenanceDate(applicationId, typeId);
        return new ResponseEntity<>(provDate, HttpStatus.OK);
    }


    @GetMapping("/prov-graph")
    ResponseEntity<ProvenanceGraph> provenanceGraph(@RequestParam("applicationId") String applicationId,
                                                    @RequestParam(name = "type", required = false) String typeId
    ) throws IOException {
        Map<IRI, IRINode> nodeMap = (typeId == null) ?
                provenanceService.generateProvenanceGraph(applicationId) :
                provenanceService.generateProvenanceGraph(applicationId, typeId);
        ProvenanceGraph graph = ProvenanceGraph.fromNodeMapToProvGraph(nodeMap);
        return new ResponseEntity<>(graph, HttpStatus.OK);
    }


    @GetMapping("/details-metadata")
    ResponseEntity<JsonNode> getApplicationDetails(@RequestParam("applicationId") String applicationId,
                                            @RequestParam(name = "format", required = false, defaultValue = "deeph") String format
    ) throws IOException, InterruptedException {
        JsonNode detailsJson = provenanceService.getApplicationDetails(applicationId, format);
        return ResponseEntity.ok().contentType(MediaType.valueOf("application/json")).body(detailsJson);
    }
}
