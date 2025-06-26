package eu.ai4eosc.provenance.api.tables.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.MediaType;

public record Type(String id, JsonNode origins, String mapping, MappingFormat mappingFormat) {

    public enum MappingFormat {

        TURTLE(RDFFormat.TURTLE, "application/x-turtle"),
        JSON(RDFFormat.JSONLD, MediaType.APPLICATION_JSON_VALUE);

        private final RDFFormat format;
        private final String contentType;

        MappingFormat(RDFFormat format, String contentType) {
            this.format = format;
            this.contentType = contentType;
        }

        public RDFFormat getFormat() {
            return format;
        }

        public String getContentType() {
            return contentType;
        }
    }

}
