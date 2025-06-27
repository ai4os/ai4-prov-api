package eu.ai4eosc.provenance.api.tables;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static eu.ai4eosc.provenance.api.tables.dto.Type.MappingFormat.TURTLE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.tables.dto.Type;
import eu.ai4eosc.provenance.api.tables.dto.Type.MappingFormat;
import eu.ai4eosc.provenance.api.tables.rmlbuilder.RMLBuilder;
import eu.ai4eosc.provenance.api.tables.rmlbuilder.RMLStruct;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeTable {
    static final Logger log = LoggerFactory.getLogger(TypeTable.class);
    static final String TABLE_NAME = "types";

    static final Field<String> ID_FIELD = DSL.field("id", String.class);
    static final Field<JSONB> ORIGINS_FIELD = DSL.field("origins", SQLDataType.JSONB);
    static final Field<String> MAPPING_FIELD = DSL.field("mapping", SQLDataType.CLOB);
    static final Field<String> MAPPING_FORMAT_FIELD = DSL.field("mapping_format", String.class);
    static final Pattern PROVENANCE_TYPE_ID_PATTERN = Pattern.compile("provenance\\((.*)\\)");

    static Map<Set<String>, String> provenanceIds = new HashMap<>();

    public static String getProvenanceIdFromSourceList(List<String> sources) {
        log.info("trying to search for a provenance with sources: {}", sources);
        return provenanceIds.get(new HashSet<>(sources));
    }

    private static void saveRMLinDB(DSLContext dsl,
                                    Type newType) {
        String newTypeId = newType.id();
        Matcher matcher = PROVENANCE_TYPE_ID_PATTERN.matcher(newTypeId);
        if (matcher.find()) {
            String[] sources = matcher.group(1).split("_");
            provenanceIds.put(new HashSet<>(Arrays.asList(sources)), newTypeId);
        }
        TypeTable.upsert(dsl, newType);

    }

    static void saveProvenanceRML(DSLContext dsl,
                           RMLBuilder rmlBuilder,
                           String coreId, List<String> extensions) throws IOException {
        RMLStruct rmlStruct = rmlBuilder.buildProvenanceRML(coreId, extensions);
        String rmlId = coreId;
        if (!extensions.isEmpty()) {
            String extensionString = String.join("_", extensions);
            rmlId = coreId + '_' + extensionString;
        }
        saveRMLinDB(dsl, new Type("provenance(%s)".formatted(rmlId),
                rmlStruct.origins(),
                rmlStruct.rmlContent(), TURTLE));
    }
    public static void initDefaultTypes(DSLContext dsl) throws IOException {
        final String coreIdDetailsJenkins = "details_jenkins";
        // Saving provenance RMLs
        RMLBuilder rmlBuilder = new RMLBuilder();
        saveProvenanceRML(dsl, rmlBuilder, coreIdDetailsJenkins, List.of());
        saveProvenanceRML(dsl, rmlBuilder, coreIdDetailsJenkins, List.of("mlflow"));
        saveProvenanceRML(dsl, rmlBuilder, coreIdDetailsJenkins, List.of("nomad"));
        saveProvenanceRML(dsl, rmlBuilder, coreIdDetailsJenkins, List.of("mlflow", "nomad"));

        // Other RMLs (just fair4ml for now)
        RMLStruct fair4mlStruct = rmlBuilder.buildSingleRML("fair4ml");
        saveRMLinDB(dsl, new Type("details_to_fair4ml", null, fair4mlStruct.rmlContent(), TURTLE));
    }

    public static Optional<Type> find(DSLContext dsl, String id) {
        var result = dsl.select(ID_FIELD, ORIGINS_FIELD, MAPPING_FIELD, MAPPING_FORMAT_FIELD)
                .from(TABLE_NAME)
                .where(ID_FIELD.eq(id))
                .fetchOne(Records.mapping(Result::new));
        return Optional.ofNullable(result).map(Result::type);
    }

    record Result(String id, JSONB origins, String mapping, String mappingFormat) {

        public Type type() {
            try {
                var originsTree = origins != null ? MAPPER.readTree(origins.data()) : null;
                var format = Optional.ofNullable(mappingFormat).map(MappingFormat::valueOf).orElse(null);
                return new Type(id, originsTree, mapping, format);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

    static Table<?> table(DSLContext dsl) {
        return dsl.meta(createTableIfNotExists(dsl))
                .getTables(TABLE_NAME).stream()
                .findAny()
                .orElseThrow();
    }

    public static int upsert(DSLContext dsl, Type type) {
        try {
            var origins = type.origins() != null ? JSONB.valueOf(MAPPER.writeValueAsString(type.origins())) : null;
            var format = Optional.ofNullable(type.mappingFormat()).map(MappingFormat::name).orElse(null);
            var table = table(dsl);
            return dsl
                    .insertInto(table, ID_FIELD, ORIGINS_FIELD, MAPPING_FIELD, MAPPING_FORMAT_FIELD)
                    .values(type.id(), origins, type.mapping(), format)
                    .onConflict(ID_FIELD)
                    .doUpdate()
                    .set(ORIGINS_FIELD, origins)
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
                .column(ORIGINS_FIELD)
                .column(MAPPING_FIELD)
                .column(MAPPING_FORMAT_FIELD)
                .constraint(DSL.primaryKey(ID_FIELD.getName()));
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

}
