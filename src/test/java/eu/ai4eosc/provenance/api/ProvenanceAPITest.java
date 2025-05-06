package eu.ai4eosc.provenance.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for Provenance Controller endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProvenanceAPITest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendMetadata() throws Exception {
        String body = """
                {
                    "applicationId": "yolov8",
                    "mlflowExperiment": {
                        "experimentId": "yolov8_L"
                    },
                    "jenkinsWorkflow": {
                        "jobName": "ai4os-yolov8-torch",
                        "jobGroup": "AI4OS-hub",
                        "jobBranch": "main",
                        "execution": 46
                    }
                }
                """;
        mockMvc.perform(post("/meta-data")
                        .content(body)
                        .contentType("application/json")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("metadata saved")));
    }

    @Test
    void getProvenance() throws Exception {
        // TODO: Complete this test with the expected RDF response
        String expectedResponseContent = """
                """;
        mockMvc.perform(get("/provenance")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(equalTo("""
                        """)));
    }
}
