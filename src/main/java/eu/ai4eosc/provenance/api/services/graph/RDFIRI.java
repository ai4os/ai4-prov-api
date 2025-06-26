package eu.ai4eosc.provenance.api.services.graph;

import org.eclipse.rdf4j.model.IRI;

public class RDFIRI {

    private String iri;

    public RDFIRI(IRI iri) {
        this.iri = iri.getNamespace() + iri.getLocalName();
    }

    public String getIri() {
        return this.iri;
    }

    @Override
    public String toString() {
        return this.iri;
    }
}
