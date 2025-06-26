package eu.ai4eosc.provenance.api.tables.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record Instance(String type, String id, LocalDateTime creationDate, JsonNode data) {
}
