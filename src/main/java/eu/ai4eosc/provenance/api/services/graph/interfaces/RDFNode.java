package eu.ai4eosc.provenance.api.services.graph.interfaces;

import eu.ai4eosc.provenance.api.services.graph.RDFType;

public interface RDFNode {
    public String getId();
    public RDFType getType();
}
