package eu.ai4eosc.provenance.api.controllers.schemas.output;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class ProvenanceGraphSerializer extends JsonSerializer<ProvenanceGraph> {
    @Override
    public void serialize(ProvenanceGraph graph, JsonGenerator gen, SerializerProvider serializer
    ) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, ProvenanceGraph.RootNode> entry : graph.provenance().entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }
}
