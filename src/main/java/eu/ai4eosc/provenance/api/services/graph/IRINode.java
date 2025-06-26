package eu.ai4eosc.provenance.api.services.graph;

import eu.ai4eosc.provenance.api.services.graph.interfaces.RDFIRINode;
import eu.ai4eosc.provenance.api.services.graph.interfaces.RDFNode;
import org.eclipse.rdf4j.model.IRI;

import java.util.*;

public class IRINode implements RDFIRINode {

    private final String id;
    private final RDFIRI iri;
    private String tag; // Short name for the iri node
    private RDFType type;
    private boolean isStarter; // use in graph for painting the nodes with this prop = true
    private boolean isDisabled; // use in graph for never painting those nodes
    Map<String, ArrayList<RDFNode>> relations = new HashMap<>(); // PredicateObject hashmap

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
    public boolean getIsStarter() { return this.isStarter; }

    @Override
    public void setIsStarter(boolean isStarter) { this.isStarter = isStarter; }

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
}
