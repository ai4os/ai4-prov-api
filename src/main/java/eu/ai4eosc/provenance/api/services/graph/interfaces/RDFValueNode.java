package eu.ai4eosc.provenance.api.services.graph.interfaces;

public interface RDFValueNode extends RDFNode {
    public String getValue();
    public String getDatatype();
}
