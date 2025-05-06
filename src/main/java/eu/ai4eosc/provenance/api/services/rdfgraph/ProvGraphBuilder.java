package eu.ai4eosc.provenance.api.services.rdfgraph;

import eu.ai4eosc.provenance.api.services.rdfgraph.graphinterfaces.RDFNode;
import org.eclipse.rdf4j.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProvGraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProvGraphBuilder.class);
    public Map<IRI, IRINode> rdfGraph = new HashMap<>();

    public ProvGraphBuilder(Model model) {
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
                // TODO: this shouldnt be set up always as true
                // But for now as the only concepts who has this predicate is for true
                // Is good enough
                iriNode.setIsDisabled(true);
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
            //logStatement(st);
        }
        log.info("[*] RDF Graph building complete!");
    }
    private void logStatement(Statement st) {
        log.info("Statement: %s".formatted(st));
//        log.info("Subject: %s \n".formatted(st.getSubject()));
//        log.info("Predicate: %s Mini: %s\n".formatted(st.getPredicate(), st.getPredicate().getLocalName()));
//        log.info("Object: %s objectClass: %s\n".formatted(st.getObject(), st.getObject().isIRI()));
//        if (!st.getObject().isIRI()) {
//            Literal lit = (Literal) st.getObject();
//            log.info("values: %s , %s".formatted(lit.getDatatype(), lit.stringValue()));
//        }
    }
    public Map<IRI, IRINode> getRdfGraph() {
        return rdfGraph;
    }

    // TODO: method for traversing the graph and serialize it to json (lista de nodos?(Desde el conjunto de nodos hasta la lista)
}
