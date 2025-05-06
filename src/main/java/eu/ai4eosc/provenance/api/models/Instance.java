package eu.ai4eosc.provenance.api.models;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public record Instance(String type, String id, LocalDate creationDate, JsonNode data) {
}
