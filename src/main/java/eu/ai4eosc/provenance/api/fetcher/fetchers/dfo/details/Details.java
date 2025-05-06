package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
public record Details (
        @JsonProperty("metadata_version")
        String metadataVersion,
        String title,
        String summary,
        String description,
        Dates dates,
        Links links,
        List<String> tags,
        List<String> tasks,
        List<String> categories,
        List<String> libraries,
        @JsonProperty("data-type")
        List<String> dataTypes,
        String license,
        String id
) {}
