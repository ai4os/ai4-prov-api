package eu.ai4eosc.provenance.api.services.rdfgraph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces.RDFIRINode;
import eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces.RDFNode;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class IRINode implements RDFIRINode {
    private static final Logger logger = LoggerFactory.getLogger(IRINode.class);
    private final String id;
    private final RDFIRI iri; // Should be id instead of iri?
    private String tag; // Short name for the iri node
    private RDFType type;
    private boolean isDisabled;

    public Map<String, ArrayList<RDFNode>> relations = new HashMap<String, ArrayList<RDFNode>>(); // PredicateObject hashmap

    public IRINode(IRI iri) {
        this.iri = new RDFIRI(iri);
        this.id = this.iri.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTag() { return this.tag; }

    @Override
    public void setTag(String tag) { this.tag = tag; }

    @Override
    public void setType(IRI type) {
        this.type = new RDFType(type);
    }

    @Override
    public RDFType getType() {
        return this.type;
    }

    @Override
    public RDFIRI getIRI() {
        return this.iri;
    }

    @Override
    public boolean getIsDisabled() { return this.isDisabled; }

    @Override
    public void setIsDisabled(boolean isDisabled) { this.isDisabled = isDisabled; }

    @Override
    public Map<String, ArrayList<RDFNode>> getRelations() {
        return this.relations;
    }

    @Override
    public void addNewConnection(String predicate, RDFNode object) {
        this.relations.computeIfAbsent(predicate,
                p -> new ArrayList<>()).add(object);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        IRINode castedObj = (IRINode) obj;
        return this.iri == castedObj.iri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri);
    }

    public static class IRINodeSerializer extends StdSerializer<IRINode> {
        public IRINodeSerializer() {
            super(IRINode.class);
        }

        @Override
        public void serialize(IRINode node, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("id", node.getId());
            gen.writeStringField("iri", node.getIRI().toString());
            gen.writeStringField("tag",node.getTag());
            gen.writeStringField("type", node.getType().toString());
            gen.writeBooleanField("disabled", node.getIsDisabled());
            gen.writeFieldName("relations");
            gen.writeStartObject();
            for (Map.Entry<String, ArrayList<RDFNode>> entry : node.getRelations().entrySet()) {
                gen.writeFieldName(entry.getKey());
                gen.writeStartArray();
                ArrayList<RDFNode> nodes = entry.getValue();
                for (RDFNode n : nodes) {
                    if (n instanceof IRINode iriNode) {
                        gen.writeStartObject();
                        gen.writeStringField("id", iriNode.getId());
                        gen.writeStringField("iri", iriNode.getIRI().toString());
                        gen.writeStringField("tag", iriNode.getTag());
                        gen.writeStringField("type", iriNode.getType().toString());
                        gen.writeEndObject();
                    }
                    if (n instanceof ValueNode valNode) {
                        gen.writeStartObject();
                        gen.writeStringField("id", valNode.getId());
                        gen.writeStringField("type", valNode.getType().toString());
                        gen.writeStringField("datatype", valNode.getDatatype());
                        gen.writeStringField("value", valNode.getValue());
                        gen.writeEndObject();
                    }
                }
                gen.writeEndArray();

            }
            gen.writeEndObject();
            gen.writeEndObject();
        }
    }
}
