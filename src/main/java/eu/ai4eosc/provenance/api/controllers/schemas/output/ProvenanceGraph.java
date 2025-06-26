package eu.ai4eosc.provenance.api.controllers.schemas.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.ai4eosc.provenance.api.services.graph.IRINode;
import eu.ai4eosc.provenance.api.services.graph.ValueNode;
import eu.ai4eosc.provenance.api.services.graph.interfaces.RDFNode;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = ProvenanceGraphSerializer.class)
public record ProvenanceGraph(
        Map<String, RootNode> provenance
) {

    public record RootNode(String id,
                           String iri,
                           String tag,
                           String type,
                           Boolean disabled,
                           Boolean starter,
                           Map<String, List<SimpleNode>> relations) {
    }

    interface SimpleNode {
    }

    record SimpleIRINode(String id,
                         String iri,
                         String tag,
                         String type) implements SimpleNode {
    }

    record SimpleValueNode(String id,
                           String type,
                           String datatype,
                           String value) implements SimpleNode {
    }

    public static ProvenanceGraph fromNodeMapToProvGraph(Map<IRI, IRINode> nodeMap) {
        HashMap<String, RootNode> provenance = new HashMap<>();
        for (Map.Entry<IRI, IRINode> entry : nodeMap.entrySet()) {
            String key = entry.getKey().toString();
            // For each relation of the IRI node
            IRINode mainNode = entry.getValue();
            HashMap<String, List<SimpleNode>> relations = new HashMap<>();
            for (Map.Entry<String, ArrayList<RDFNode>> relationEntry : mainNode.getRelations().entrySet()) {
                String relation = relationEntry.getKey();
                ArrayList<SimpleNode> mappedRelations = new ArrayList<>();
                ArrayList<RDFNode> nodes = relationEntry.getValue();
                for (RDFNode node : nodes) {
                    if (node instanceof IRINode irin) {
                        mappedRelations
                                .add(
                                        new SimpleIRINode(irin.getId(),
                                                irin.getIRI().toString(),
                                                irin.getTag(),
                                                irin.getType().toString()
                                        ));
                    }
                    if (node instanceof ValueNode valuen) {
                        mappedRelations
                                .add(
                                        new SimpleValueNode(valuen.getId(),
                                                valuen.getType().toString(),
                                                valuen.getDatatype(),
                                                valuen.getValue()
                                        ));
                    }
                }
                relations.put(relation, mappedRelations);
            }
            RootNode rootNode =
                    new RootNode(mainNode.getId(),
                            mainNode.getIRI().toString(),
                            mainNode.getTag(),
                            mainNode.getType().toString(),
                            mainNode.getIsDisabled(),
                            mainNode.getIsStarter(),
                            relations);

            provenance.put(key, rootNode);
        }

        return new ProvenanceGraph(provenance);
    }
}

