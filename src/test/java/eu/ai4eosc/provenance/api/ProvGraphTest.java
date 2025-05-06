package eu.ai4eosc.provenance.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.services.RDFGraphService;
import eu.ai4eosc.provenance.api.services.RDFMappingService;
import eu.ai4eosc.provenance.api.services.rdfgraph.ProvGraphBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Test RML mapping to Provenance Frontend Graph JSON format
 */
public class ProvGraphTest {
    private static final Logger logger = LoggerFactory.getLogger(AI4EOSCMetadataTest.class);
    private static final RDFMappingService mappingService = new RDFMappingService();
    private static final RDFGraphService rdfGraphService = new RDFGraphService();
    private static final String RML_MAPPING_FILE = "/rmlmappings/test1_jenkins_mlflow_rml.ttl";
    private static final String DATA_FOLDER = "/data/yolov8/";
    private static final String OUTPUT_FOLDER = "/output/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void mappingTest() throws Exception {
        Map<String, InputStream> data = getStringInputStreamMap();

        InputStream mappings = new ClassPathResource(RML_MAPPING_FILE).getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mappingService.mapping(mappings, RDFFormat.TURTLE, data, output, RDFFormat.JSONLD);
        ProvGraphBuilder provGraphBuilder = rdfGraphService.buildGraph(new ByteArrayInputStream(output.toByteArray())).orElseThrow();
        try {
            // No lo escribe como en la response porque no tiene configurados los serializer
            Files.writeString(Paths.get("test-output/ai4eosc-metadata-provgraph.json"),
                    MAPPER.writeValueAsString(provGraphBuilder.getRdfGraph()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, InputStream> getStringInputStreamMap() throws IOException {
        InputStream rmlConstants = new ClassPathResource(DATA_FOLDER + "rmlconstants.json").getInputStream();
        InputStream detailsMetaData = new ClassPathResource(DATA_FOLDER + "metadata_details.json").getInputStream();
        InputStream jenkinsMetaData = new ClassPathResource(DATA_FOLDER + "metadata_jenkins.json").getInputStream();
        InputStream mlflowMetaData = new ClassPathResource(DATA_FOLDER + "metadata_mlflow.json").getInputStream();

        return Map.of(
                "constants", rmlConstants,
                "details-metadata", detailsMetaData,
                "jenkins-metadata", jenkinsMetaData,
                "mlflow-metadata", mlflowMetaData
        );
    }
}
