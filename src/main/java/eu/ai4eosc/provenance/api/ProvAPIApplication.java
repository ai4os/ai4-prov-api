package eu.ai4eosc.provenance.api;

import eu.ai4eosc.provenance.api.tables.InstanceTable;
import eu.ai4eosc.provenance.api.services.RDFMappingService;
import eu.ai4eosc.provenance.api.tables.TypeTable;
import org.jooq.DSLContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProvAPIApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProvAPIApplication.class, args);
    }

    @Bean
    static RDFMappingService rdfMappingService() {
        return new RDFMappingService();
    }

    @Bean
    static DbInitializer dbInitializer(DSLContext dsl) {

        return new DbInitializer(dsl);
    }

    record DbInitializer(DSLContext dsl) implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments args) {

            TypeTable.createTableIfNotExists(dsl).execute();
            TypeTable.initDefaultTypes(dsl);
            InstanceTable.createTableIfNotExists(dsl).execute();
        }
    }
}
