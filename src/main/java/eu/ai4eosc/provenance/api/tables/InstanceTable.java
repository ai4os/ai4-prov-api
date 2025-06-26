package eu.ai4eosc.provenance.api.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ai4eosc.provenance.api.tables.dto.Instance;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class InstanceTable {
	
	static final String TABLE_NAME = "instances";
	static final Field<String> ID_FIELD = DSL.field("id", String.class);
	static final Field<String> TYPE_FIELD = DSL.field("type", String.class);
	static final Field<LocalDateTime> CREATION_DATE_FIELD = DSL.field("cdate", LocalDateTime.class);
	static final Field<JSONB> DATA_FIELD = DSL.field("data", SQLDataType.JSONB);
	
	public static Optional<Instance> find(DSLContext dsl, String type, String id) {
		var result = dsl.select(TYPE_FIELD, ID_FIELD, CREATION_DATE_FIELD, DATA_FIELD)
			.from(TABLE_NAME)
			.where(TYPE_FIELD.eq(type).and(ID_FIELD.eq(id)))
			.fetchOne(Records.mapping(Result::new));
		return Optional.ofNullable(result)
			.map(Result::instance);
	}

	public static Optional<Instance> findMoreRecent(DSLContext dsl, String id) {
		var subquery = dsl.select(DSL.max(CREATION_DATE_FIELD))
				.from(TABLE_NAME)
				.where(ID_FIELD.eq(id));

		var result = dsl.select(TYPE_FIELD, ID_FIELD, CREATION_DATE_FIELD, DATA_FIELD)
				.from(TABLE_NAME)
				.where(ID_FIELD.eq(id).and(CREATION_DATE_FIELD.eq(subquery)))
				.fetchOne(Records.mapping(Result::new));

		return Optional.ofNullable(result)
				.map(Result::instance);
	}
	record Result (String type, String id, LocalDateTime creationDate, JSONB data) {

		public Instance instance() {
            try {
				var dataTree = data.data() != null ?
						MAPPER.readTree(data.data()) : null;
				return new Instance(type, id, creationDate, dataTree);
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

	public static int upsert(DSLContext dsl, Instance instance) {
        try {
            var data = instance.data() != null ?
					JSONB.valueOf(MAPPER.writeValueAsString(instance.data())) : null;
			var table = table(dsl);
			return dsl
					.insertInto(table, TYPE_FIELD, ID_FIELD, CREATION_DATE_FIELD, DATA_FIELD)
					.values(instance.type(), instance.id(), LocalDateTime.now(), data)
					.onConflict(TYPE_FIELD, ID_FIELD)
					.doUpdate()
					.set(DATA_FIELD, data)
					.execute();
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
	}

	public static Query createTableIfNotExists(DSLContext dsl) {
		return dsl.createTableIfNotExists(TABLE_NAME)
			.column(TYPE_FIELD)
			.column(ID_FIELD)
			.column(CREATION_DATE_FIELD)
			.column(DATA_FIELD)
			.constraint(DSL.primaryKey(TYPE_FIELD.getName(), ID_FIELD.getName()));
	}
	
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
	
}
