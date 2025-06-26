package eu.ai4eosc.provenance.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.services.GraphService;
import eu.ai4eosc.provenance.api.services.RMLService;
import eu.ai4eosc.provenance.api.utils.ISReader;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Test RML mapping to Provenance Frontend Graph JSON format
 */
@EnabledIf("eu.ai4eosc.provenance.api.EnabledIf#runIntegration")
public class GraphBuilderTest {
    private static final Logger log = LoggerFactory.getLogger(GraphBuilderTest.class);

    private static final RMLService mappingService = new RMLService();
    private static final GraphService graphService = new GraphService();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ISReader isReader = new ISReader();

    @Test
    public void graphBuildingTest() throws IOException {
        List<String> test_examples = List.of(
                buildExamplePath("details_jenkins_mlflow_nomad"),
                buildExamplePath("fair4ml-yolov8")
        );
        for (String example : test_examples) {
            runExample(example);
        }
    }

    private String buildExamplePath(String subDir) {
        return "/examples/%s".formatted(subDir);
    }

    private void runExample(String examplePath) throws IOException {
        log.info("Executing example -> {}", examplePath);
        // Reading Data
        String dataContent = isReader.readResourceFileAsString(examplePath + "/data.json");
        JsonNode dataTree = MAPPER.readTree(dataContent);
        // Reading rml file
        String rml = isReader.readResourceFileAsString(examplePath + "/rml.ttl");
        // Generating RDF
        String rdf = mappingService.mapping(rml, RDFFormat.TURTLE, dataTree, RDFFormat.JSONLD);
        // Generates Graph, and it does not return in any way, this is just to check that it runs
        graphService.buildGraph(rdf);
    }
}
