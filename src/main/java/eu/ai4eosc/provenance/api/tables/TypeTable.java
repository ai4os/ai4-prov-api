package eu.ai4eosc.provenance.api.tables;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static eu.ai4eosc.provenance.api.models.Type.MappingFormat.TURTLE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.models.Type;
import eu.ai4eosc.provenance.api.models.Type.MappingFormat;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;


public class TypeTable {
	static final String TABLE_NAME = "types";
	
	static final Field<String> ID_FIELD = DSL.field("id", String.class);
	static final Field<JSONB> SCHEMA_FIELD = DSL.field("data", SQLDataType.JSONB);
	static final Field<String> MAPPING_FIELD = DSL.field("mapping", SQLDataType.CLOB);
	static final Field<String> MAPPING_FORMAT_FIELD = DSL.field("mapping_format", String.class);

	public static void initDefaultTypes(DSLContext dsl) {
		// Loads jenkins_mlflow validation schema
		try {
			String pathJSONSchema = "/dbinit/jenkins-mlflow/jsonschema/validate.json";
			InputStream jenkinsMlflowMetadataSchema = new ClassPathResource(pathJSONSchema).getInputStream();
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonSchemaNode = objectMapper.readTree(jenkinsMlflowMetadataSchema);

			// Store jenkins-mlflow-details RML
			String jenkinsMLflowDetailsRMLPath = "/dbinit/jenkins-mlflow/rml/rml.ttl";
			InputStream jenkinsMlflowRML = new ClassPathResource(jenkinsMLflowDetailsRMLPath).getInputStream();
			String jenkinsMlflowRMLString = new String(jenkinsMlflowRML.readAllBytes(), UTF_8);
			Type jenkinsMlflowRMLType = new Type("details_jenkins_mlflow", jsonSchemaNode, jenkinsMlflowRMLString, TURTLE);
			TypeTable.upsert(dsl, jenkinsMlflowRMLType);

			// Store Fair4ML (rml mapping)
			String fair4MLRMLPath = "/dbinit/fair4ml/rml/rml.ttl";
			InputStream fair4MLRML = new ClassPathResource(fair4MLRMLPath).getInputStream();
			String fair4MLRMLString = new String(fair4MLRML.readAllBytes(), UTF_8);
			Type fair4MLRMLType = new Type("details_fair4ml", null, fair4MLRMLString, TURTLE);
			TypeTable.upsert(dsl, fair4MLRMLType);

			// Store details-jenkins-mlflow-nomad
			String detailsJenkinsMlflowNomadRMLPath = "/dbinit/jenkins-mlflow-nomad/rml/rml.ttl";
			InputStream detailsJenkinsMlflowNomadRML = new ClassPathResource(detailsJenkinsMlflowNomadRMLPath).getInputStream();
			String detailsJenkinsMlflowNomadRMLString = new String(detailsJenkinsMlflowNomadRML.readAllBytes(), UTF_8);
			Type detailsJenkinsMlflowNomadRMLType = new Type("details_jenkins_mlflow_nomad", null, detailsJenkinsMlflowNomadRMLString, TURTLE);
			TypeTable.upsert(dsl, detailsJenkinsMlflowNomadRMLType);

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static Optional<Type> find(DSLContext dsl, String id) {
		var result = dsl.select(ID_FIELD, SCHEMA_FIELD, MAPPING_FIELD, MAPPING_FORMAT_FIELD)
			.from(TABLE_NAME)
			.where(ID_FIELD.eq(id))
			.fetchOne(Records.mapping(Result::new));
		return Optional.ofNullable(result).map(Result::type);
	}
	
	record Result (String id, JSONB schema, String mapping, String mappingFormat) {

		public Type type() {
            try {
				var schemaTree = schema != null ?
						MAPPER.readTree(schema.data()) : null;
				var format = Optional.ofNullable(mappingFormat).map(MappingFormat::valueOf).orElse(null);
				return new Type(id, schemaTree, mapping, format);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
		}
		
	}

	static Table<?> table(DSLContext dsl){
		return dsl.meta(createTableIfNotExists(dsl))
				.getTables(TABLE_NAME).stream()
				.findAny()
				.orElseThrow();
	}

	public static int upsert(DSLContext dsl, Type type) {
        try {
            var schema = type.schema() != null ?
					JSONB.valueOf(MAPPER.writeValueAsString(type.schema())) : null;
			var format = Optional.ofNullable(type.mappingFormat()).map(MappingFormat::name).orElse(null);
			var table = table(dsl);
			return dsl
					.insertInto(table, ID_FIELD, SCHEMA_FIELD, MAPPING_FIELD, MAPPING_FORMAT_FIELD)
					.values(type.id(), schema, type.mapping(), format)
					.onConflict(ID_FIELD)
					.doUpdate()
					.set(SCHEMA_FIELD, schema)
					.set(MAPPING_FIELD, type.mapping())
					.set(MAPPING_FORMAT_FIELD, format)
					.execute();
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
	}

	public static Query createTableIfNotExists(DSLContext dsl) {
		return dsl.createTableIfNotExists(TABLE_NAME)
			.column(ID_FIELD)
			.column(SCHEMA_FIELD)
			.column(MAPPING_FIELD)
			.column(MAPPING_FORMAT_FIELD)
			.constraint(DSL.primaryKey(ID_FIELD.getName()));
	}
	
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
	
}
