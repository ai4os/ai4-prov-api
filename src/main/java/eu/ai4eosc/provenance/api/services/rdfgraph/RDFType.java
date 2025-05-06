package eu.ai4eosc.provenance.api.services.rdfgraph;

import org.eclipse.rdf4j.model.IRI;

public class RDFType {
    String type;

    public RDFType(String rawType) {
        this.type = rawType;
    }
    public RDFType(IRI iri) {
        this.type = iri.getLocalName();
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return this.type;
    }
}
