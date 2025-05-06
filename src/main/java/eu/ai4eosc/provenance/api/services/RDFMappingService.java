package eu.ai4eosc.provenance.api.services;

import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.logicalsourceresolver.CsvResolver;
import io.carml.logicalsourceresolver.JsonPathResolver;
import io.carml.logicalsourceresolver.XPathResolver;
import io.carml.util.RmlMappingLoader;
import io.carml.vocab.Rdf;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class RDFMappingService {
	public void mapping(InputStream mapping, RDFFormat mappingFormat, InputStream data, OutputStream output, RDFFormat outputFormat) {
		mapping(mapping, mappingFormat, Map.of("data", data), output, outputFormat);
	}

	public void mapping(InputStream mapping, RDFFormat mappingFormat, Map<String,InputStream> data, OutputStream output, RDFFormat outputFormat) {
		var mapper = mapper(mapping,mappingFormat);
		var model = mapper.mapToModel(data);
		Rio.write(model, output, outputFormat);
	}
	
	private RdfRmlMapper mapper(InputStream mapping, RDFFormat mappingFormat) {
		var mappings = RmlMappingLoader.build().load(mappingFormat, mapping);
		return RdfRmlMapper.builder()
			.triplesMaps(mappings)
			.setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance) // Depending of what type of reference formulation use one resolver or another
			.setLogicalSourceResolver(Rdf.Ql.XPath, XPathResolver::getInstance)
			.setLogicalSourceResolver(Rdf.Ql.Csv, CsvResolver::getInstance)
			.build();

	}

}
