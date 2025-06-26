package eu.ai4eosc.provenance.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.logicalsourceresolver.CsvResolver;
import io.carml.logicalsourceresolver.JsonPathResolver;
import io.carml.logicalsourceresolver.XPathResolver;
import io.carml.util.RmlMappingLoader;
import io.carml.vocab.Rdf;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RMLService {

	public String mapping(String rml, RDFFormat mappingFormat, JsonNode dataJSON, RDFFormat outputFormat) throws IOException {
		try (var mappingIs = new ByteArrayInputStream(rml.getBytes());
			 var dataMap = DataMap.lookup(dataJSON);
			 var baos = new ByteArrayOutputStream();) {
			var mapper = mapper(mappingIs, mappingFormat);
			var model = mapper.mapToModel(dataMap.getData());
			Rio.write(model, baos, outputFormat);
			return baos.toString(StandardCharsets.UTF_8);
		}
	}
	
	private RdfRmlMapper mapper(InputStream mapping, RDFFormat mappingFormat) {
		var mappings = RmlMappingLoader.build().load(mappingFormat, mapping);
		return RdfRmlMapper.builder()
			.triplesMaps(mappings)
			.setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance) // Depends on what type of reference formulation use one resolver or another
			.setLogicalSourceResolver(Rdf.Ql.XPath, XPathResolver::getInstance)
			.setLogicalSourceResolver(Rdf.Ql.Csv, CsvResolver::getInstance)
			.build();

	}

}
