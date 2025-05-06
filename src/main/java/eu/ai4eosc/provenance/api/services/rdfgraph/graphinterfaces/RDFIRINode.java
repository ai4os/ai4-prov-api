package eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces;

import eu.ai4eosc.provenance.api.services.rdfgraph.RDFIRI;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.Map;

public interface RDFIRINode extends RDFNode {
    public RDFIRI getIRI();
    public void setType(IRI type);
    public boolean getIsDisabled();
    public void setIsDisabled(boolean isDisabled);
    public String getTag();
    public void setTag(String tag);
    public Map<String, ArrayList<RDFNode>> getRelations();
    public void addNewConnection(String predicate, RDFNode object);
}
