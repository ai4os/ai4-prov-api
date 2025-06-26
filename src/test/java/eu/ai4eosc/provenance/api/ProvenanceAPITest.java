package eu.ai4eosc.provenance.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

/**
 * Tests for Provenance Controller endpoints
 */
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@EnabledIf("eu.ai4eosc.provenance.api.EnabledIf#runIntegration")
@AutoConfigureMockMvc
public class ProvenanceAPITest {


    @Autowired
    private MockMvc mockMvc;

    // This test cant be executed because we cant expose credentials here
    // And credentials are a must for POST /meta-data
//    @Test
//    void sendMetadata() throws Exception {
//
//        String body = """
//                {
//                    "applicationId": "ai4os-demo-app",
//                    "jenkinsWorkflow": {
//                        "name": "ai4os-demo-app",
//                        "group": "AI4OS-hub",
//                        "branch": "main",
//                        "build": 51
//                    }
//                }
//                """;
//        mockMvc.perform(post("/meta-data")
//                        .header("X-API-KEY", "password")
//                        .content(body)
//                        .contentType("application/json")).andDo(print()).andExpect(status().isOk())
//                .andExpect(content().string(containsString("fetching provenance...")));
//    // TODO: assert that there is a row inserted in testcontainers postgredb
//    }

    @DynamicPropertySource
    static void setup(DynamicPropertyRegistry registry) throws IOException {
        PropsConfigurer.setup(registry, new DbPropsConfigurer());
    }

}
