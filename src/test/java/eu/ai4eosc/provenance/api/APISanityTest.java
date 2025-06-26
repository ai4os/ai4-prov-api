package eu.ai4eosc.provenance.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = ProvAPIApplication.class, webEnvironment = RANDOM_PORT, properties = {

})
@ActiveProfiles(profiles = {"test"})
@EnabledIf("eu.ai4eosc.provenance.api.EnabledIf#runIntegration")
public class APISanityTest {

    @Test
    void contextLoads() {

    }

    @DynamicPropertySource
    static void setup(DynamicPropertyRegistry registry) throws IOException {
        PropsConfigurer.setup(registry, new DbPropsConfigurer());
    }

}
