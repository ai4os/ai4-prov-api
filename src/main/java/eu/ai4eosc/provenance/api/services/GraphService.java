package eu.ai4eosc.provenance.api.services;

import eu.ai4eosc.provenance.api.services.graph.GraphBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class GraphService {
    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    public GraphBuilder buildGraph(String rdf) throws IOException {
        return buildGraph(rdf, RDFFormat.JSONLD);
    }

    public GraphBuilder buildGraph(String rdf, RDFFormat rdfFormat) throws IOException {
        log.info("Calling Graph Builder...");
        Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()) , rdfFormat);
        return new GraphBuilder(model);
    }
}
