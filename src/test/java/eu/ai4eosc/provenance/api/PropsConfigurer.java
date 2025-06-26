package eu.ai4eosc.provenance.api;

import org.springframework.test.context.DynamicPropertyRegistry;

import java.io.IOException;

@FunctionalInterface
public interface PropsConfigurer {

    void setupProperties(DynamicPropertyRegistry registry) throws IOException;

    static void setup(DynamicPropertyRegistry registry, PropsConfigurer... configurers) throws IOException {
        for(var configurer : configurers) {
            configurer.setupProperties(registry);
        }
    }

}

