package eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces;

public interface RDFValueNode extends RDFNode {
    public String getValue();
    public String getDatatype();
}
