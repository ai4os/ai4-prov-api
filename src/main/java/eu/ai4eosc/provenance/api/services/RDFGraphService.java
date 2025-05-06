package eu.ai4eosc.provenance.api.services;

import eu.ai4eosc.provenance.api.services.rdfgraph.ProvGraphBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class RDFGraphService {
    private static final Logger logger = LoggerFactory.getLogger(RDFGraphService.class);

    public Optional<ProvGraphBuilder> buildGraph(InputStream rdfIs) {
        return buildGraph(rdfIs, RDFFormat.JSONLD);
    }

    public Optional<ProvGraphBuilder> buildGraph(InputStream rdfIs, RDFFormat rdfFormat) {
        try {
            Model model = Rio.parse(rdfIs, rdfFormat);
            return Optional.of(new ProvGraphBuilder(model));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
