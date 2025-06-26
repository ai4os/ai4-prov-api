package eu.ai4eosc.provenance.api.services;

import static eu.ai4eosc.provenance.api.tables.dto.Type.MappingFormat.JSON;
import static eu.ai4eosc.provenance.api.tables.dto.Type.MappingFormat.TURTLE;
import static eu.ai4eosc.provenance.api.tables.dto.Type.MappingFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ai4eosc.provenance.api.tables.dto.Instance;
import eu.ai4eosc.provenance.api.tables.dto.Type;
import eu.ai4eosc.provenance.api.tables.InstanceTable;
import eu.ai4eosc.provenance.api.tables.TypeTable;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MetadataService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private RMLService rmlService;
    @Autowired
    private DSLContext dsl;

    public static class TypeRMLNotFoundException extends IOException {
        public TypeRMLNotFoundException(String type) {
            super("No type %s found".formatted(type));
        }
    }

    public void upsertInstance(String typeId, String id, JsonNode jsonData) throws TypeRMLNotFoundException {
        log.info("upserting... {}", typeId);
        Optional<Type> type = TypeTable.find(dsl, typeId);
        if (type.isEmpty()) throw new TypeRMLNotFoundException(typeId);
        InstanceTable.upsert(dsl, new Instance(typeId, id, LocalDateTime.now(), jsonData));
    }

    public String rmlMapping(String typeId, String applicationId) throws IOException {
        return rmlMapping(typeId, applicationId, MappingFormat.JSON);
    }

    /**
     * Get the rml mapping for a specific metadata block (json)
     * saved in pg database.
     *
     * @param typeId,        rml metadata type
     * @param applicationId, metadata block id
     * @return rdf graph in .ttl format
     */
    public String rmlMapping(String typeId, String applicationId, MappingFormat outputFormat) throws IOException {
        Instance instance = InstanceTable.find(dsl, typeId, applicationId).orElseThrow();
        JsonNode jsonData = instance.data();
        Type typeObject = TypeTable.find(dsl, typeId).orElseThrow();
        JsonNode origins = typeObject.origins();

        if (origins != null) {
            // Load origins of type inside data json
            ObjectNode objectNode = (ObjectNode) jsonData;
            objectNode.set("origins", origins);
        }
        String rml = typeObject.mapping();

        return rmlService.mapping(rml, TURTLE.getFormat(),
                jsonData, outputFormat.getFormat());
    }

    public String directRmlMapping(String typeId, JsonNode jsonData) throws IOException {
        Type typeObject = TypeTable.find(dsl, typeId).orElseThrow();
        String rml = typeObject.mapping();
        return rmlService.mapping(rml, TURTLE.getFormat(),
                jsonData, JSON.getFormat());
    }
}
