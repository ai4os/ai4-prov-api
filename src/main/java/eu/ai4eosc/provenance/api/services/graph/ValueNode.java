package eu.ai4eosc.provenance.api.services.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ai4eosc.provenance.api.services.graph.interfaces.RDFValueNode;
import org.eclipse.rdf4j.model.IRI;

import java.util.UUID;

public class ValueNode implements RDFValueNode {
    private final String id;
    private final RDFType type;
    private final RDFType datatype;
    private final String value;
    public ValueNode(IRI type, String value) {
        this.datatype = new RDFType(type);
        this.value = value;
        this.id = UUID.randomUUID().toString();
        this.type = new RDFType("Literal");
    }

    @Override
    @JsonProperty("datatype")
    public String getDatatype() {
        return this.datatype.getType();
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public RDFType getType() {
        return this.type;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
