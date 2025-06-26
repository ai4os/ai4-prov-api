package eu.ai4eosc.provenance.api.tables.rmlbuilder;

import com.fasterxml.jackson.databind.JsonNode;

public record RMLStruct(JsonNode origins, String rmlContent) {
}
