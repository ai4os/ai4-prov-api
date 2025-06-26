package eu.ai4eosc.provenance.api;

import java.util.Map;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DbPropsConfigurer implements PropsConfigurer {

    static final String DB_NAME = "test";
    static final String DB_USERNAME = "test";
    static final String DB_PASSWORD = "test";

    private final GenericContainer<?> postgres = new GenericContainer<>(DockerImageName.parse("postgres:16"))
            .withEnv(Map.of(
                    "POSTGRES_DB", DB_NAME,
                    "POSTGRES_USER", DB_USERNAME,
                    "POSTGRES_PASSWORD", DB_PASSWORD
            ))
            .withExposedPorts(5432);

    @Override
    public void setupProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("db.host", postgres::getHost);
        registry.add("db.port", postgres::getFirstMappedPort);
        registry.add("db.name", () -> DB_NAME);
        registry.add("db.username", () -> DB_USERNAME);
        registry.add("db.password", () -> DB_PASSWORD);
    }

}

