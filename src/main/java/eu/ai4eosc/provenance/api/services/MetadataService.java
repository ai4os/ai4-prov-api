package eu.ai4eosc.provenance.api.services;

import static eu.ai4eosc.provenance.api.models.Type.MappingFormat.JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static eu.ai4eosc.provenance.api.models.Type.MappingFormat.TURTLE;
import static eu.ai4eosc.provenance.api.models.Type.MappingFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import eu.ai4eosc.provenance.api.models.Instance;
import eu.ai4eosc.provenance.api.models.Type;
import eu.ai4eosc.provenance.api.tables.InstanceTable;
import eu.ai4eosc.provenance.api.tables.TypeTable;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class MetadataService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private RDFMappingService rdfMappingService;
    @Autowired
    private DSLContext dsl;

    public static class TypeRMLNotFoundException extends Exception {
        public TypeRMLNotFoundException(String type) {
            super("No type %s found".formatted(type));
        }
    }

    public int upsertInstance(String type, String id, JsonNode jsonData) throws TypeRMLNotFoundException {
        Optional<Type> typeMeta = TypeTable.find(dsl, type);
        if (typeMeta.isEmpty()) throw new TypeRMLNotFoundException(type);
        JsonNode schema = typeMeta.orElseThrow().schema();
        if (schema != null) { // If the schema is define it validates the data
            // Validation jsonData with jsonSchema from type
            var jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                    .getSchema(typeMeta.orElseThrow().schema());
            var validation = jsonSchema.validate(jsonData);
            var errors = validation.stream()
                    .map(ValidationMessage::getMessage)
                    .toList();
            if (!errors.isEmpty()) {
                for (String e : errors) log.error(e);
                throw new JsonSchemaException("Error Validating JSON data");
            }
            ;
        }
        return InstanceTable.upsert(dsl, new Instance(type, id, LocalDate.now(), jsonData));
    }
    public Optional<ByteArrayInputStream> rmlMapping(String typeId, String applicationId) {
        return rmlMapping(typeId, applicationId, MappingFormat.JSON);
    }
    /**
     * Get the rml mapping for a specific metadata block (json)
     * saved in pg database.
     *
     * @param typeId, rml metadata type
     * @param applicationId, metadata block id
     * @return rdf graph in .ttl format
     */
    public Optional<ByteArrayInputStream> rmlMapping(String typeId, String applicationId, MappingFormat outputFormat) {
        Instance instance = InstanceTable.find(dsl, typeId, applicationId).orElseThrow();
        JsonNode jsonData = instance.data();
        Type typeObject = TypeTable.find(dsl, typeId).orElseThrow();
        String rdfMapping = typeObject.mapping();
        try (
                var mappingIs = new ByteArrayInputStream(rdfMapping.getBytes(UTF_8));
                var dataLookup = DataMap.lookup(jsonData);
                var resultOs = new ByteArrayOutputStream();
        ) {
            var mappingFormat = Optional.ofNullable(typeObject.mappingFormat()).orElse(TURTLE);
            rdfMappingService.mapping(mappingIs, mappingFormat.getFormat(),
                    dataLookup.getData(), resultOs, outputFormat.getFormat());
            return Optional.of(new ByteArrayInputStream(resultOs.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<ByteArrayInputStream> directRmlMapping(String typeId, JsonNode data) {
        Type typeObject = TypeTable.find(dsl, typeId).orElseThrow();
        String rml = typeObject.mapping();
        try (
                var mappingIs = new ByteArrayInputStream(rml.getBytes(UTF_8));
                var dataLookup = DataMap.simpleLookup(data);
                var resultOs = new ByteArrayOutputStream();
        ) {
            rdfMappingService.mapping(mappingIs, TURTLE.getFormat(),
                    dataLookup.getData(), resultOs, JSON.getFormat());
            return Optional.of(new ByteArrayInputStream(resultOs.toByteArray()));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
