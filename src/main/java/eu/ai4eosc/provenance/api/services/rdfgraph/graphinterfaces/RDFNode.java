package eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces;

import eu.ai4eosc.provenance.api.services.rdfgraph.RDFType;

public interface RDFNode {
    public String getId();
    public RDFType getType();
}
