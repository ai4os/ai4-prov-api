package eu.ai4eosc.provenance.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import eu.ai4eosc.provenance.api.services.RDFMappingService;
import org.apache.commons.io.IOUtils;
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
 * Test RML mapping to RDF JSONLD
 */
public class AI4EOSCMetadataTest {
    private static final Logger logger = LoggerFactory.getLogger(AI4EOSCMetadataTest.class);
    private static final RDFMappingService mappingService = new RDFMappingService();
    private static final String RML_MAPPING_FILE = "/rmlmappings/test1_jenkins_mlflow_rml.ttl";
    private static final String DATA_FOLDER = "/data/yolov8/";
    private static final String OUTPUT_FOLDER = "/output/";

    @Test
    public void mappingTest() throws Exception {
        Map<String, InputStream> data = getStringInputStreamMap();

        InputStream mappings = new ClassPathResource(RML_MAPPING_FILE).getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mappingService.mapping(mappings, RDFFormat.TURTLE, data, output, RDFFormat.JSONLD);
        try {
            Files.writeString(Paths.get("test-output/ai4eosc-metadata.json"), output.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, InputStream> getStringInputStreamMap() throws IOException {
        InputStream rmlConstants = new ClassPathResource(DATA_FOLDER + "rmlconstants.json").getInputStream();
        InputStream detailsMetaData = new ClassPathResource(DATA_FOLDER + "metadata_details.json").getInputStream();
        InputStream jenkinsMetaData = new ClassPathResource(DATA_FOLDER + "metadata_jenkins.json").getInputStream();
        InputStream mlflowMetaData = new ClassPathResource(DATA_FOLDER + "metadata_mlflow.json").getInputStream();
        InputStream nomadMetaData = new ClassPathResource(DATA_FOLDER + "metadata_nomad.json").getInputStream();
        return Map.of(
                "constants", rmlConstants,
                "details-metadata", detailsMetaData,
                "jenkins-metadata", jenkinsMetaData,
                "mlflow-metadata", mlflowMetaData,
                "nomad-metadata", nomadMetaData
        );
    }
}
