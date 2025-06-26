package eu.ai4eosc.provenance.api;

import eu.ai4eosc.provenance.api.tables.InstanceTable;
import eu.ai4eosc.provenance.api.services.RMLService;
import eu.ai4eosc.provenance.api.tables.TypeTable;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@ConfigurationPropertiesScan
@EnableAsync
public class ProvAPIApplication {
    static final Logger log = LoggerFactory.getLogger(ProvAPIApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProvAPIApplication.class, args);
    }

    @Bean
    static RMLService rdfMappingService() {
        return new RMLService();
    }

    @Bean
    static DbInitializer dbInitializer(DSLContext dsl) {
        return new DbInitializer(dsl);
    }

    record DbInitializer(DSLContext dsl) implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments args) {

            TypeTable.createTableIfNotExists(dsl).execute();
            try {
                TypeTable.initDefaultTypes(dsl);
            } catch (Exception e) {
                log.error("Something went wrong initializing database:", e);
            }
            InstanceTable.createTableIfNotExists(dsl).execute();
        }
    }
}
