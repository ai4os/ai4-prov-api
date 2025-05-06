package eu.ai4eosc.provenance.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.models.Type;
import eu.ai4eosc.provenance.api.services.RDFMappingService;
import io.carml.util.Mapping;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * From Details application metadata json to Fair4ml RDF using RML mapping
 */
public class Fair4mlTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DATA_FOLDER = "/data/fair4ml/";
    private static final String RML_MAPPING_FILE = "/rmlmappings/test_fair4ml.rml";
    private static final RDFMappingService mappingService = new RDFMappingService();
    @Test
    public void fair4mltest() throws IOException {
        // Read detail application metadata json in deeph format
        InputStream detailsJson = new ClassPathResource(DATA_FOLDER + "test1(deephformat).json").getInputStream();
        Map<String, InputStream> data = Map.of("details-metadata", detailsJson);
        // Read fair4ml rml
        InputStream fair4mlRML = new ClassPathResource(RML_MAPPING_FILE).getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        mappingService.mapping(fair4mlRML, RDFFormat.TURTLE,
                data, output, RDFFormat.JSONLD);
        try {
            Files.writeString(Paths.get("test-output/details-fair4ml-rdf.json"), output.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
