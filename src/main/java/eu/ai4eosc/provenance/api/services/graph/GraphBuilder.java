package eu.ai4eosc.provenance.api.services.graph;

import eu.ai4eosc.provenance.api.services.graph.interfaces.RDFNode;
import org.eclipse.rdf4j.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(GraphBuilder.class);
    Map<IRI, IRINode> rdfGraph = new HashMap<>();

    public GraphBuilder(Model model) {
        for (Statement st : model) {
            IRI subj = (IRI) st.getSubject();
            IRI pred = st.getPredicate();
            Value obj = st.getObject();

            IRINode iriNode = rdfGraph.computeIfAbsent(subj, IRINode::new);

            if (pred.getLocalName().equals("type")) {
                iriNode.setType((IRI) obj);
                continue;
            }

            if (pred.getLocalName().equals("isDisabled")) {
                // TODO: this shouldn't be set up always as true bcs we are not looking at the actual value of the term
                // But for now as the only concepts who has this predicate is for true
                // Is good enough
                iriNode.setIsDisabled(true);
                continue;
            }

            if (pred.getLocalName().equals("isStarter")) {
                // TODO: this shouldn't be set up always as true bcs we are not looking at the actual value of the term
                // But for now as the only concepts who has this predicate is for true
                // Is good enough
                iriNode.setIsStarter(true);
                continue;
            }

            if (pred.getLocalName().equals("tag")) {
                String tag = obj.stringValue();
                iriNode.setTag(tag);
                continue;
            }

            RDFNode linkedNode;
            if (obj.isIRI()) {
                IRI linkedIRI = (IRI) obj;
                linkedNode = rdfGraph.computeIfAbsent(linkedIRI, IRINode::new);
            }
            else {
                Literal lit = (Literal) obj;
                linkedNode = new ValueNode(lit.getDatatype(), lit.stringValue());
            }
            iriNode.addNewConnection(st.getPredicate().toString(), linkedNode);
        }
        log.info("[*] RDF Graph building complete!");
    }
    public Map<IRI, IRINode> getRdfGraph() {
        return rdfGraph;
    }
}
