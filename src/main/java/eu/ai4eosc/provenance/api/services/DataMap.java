package eu.ai4eosc.provenance.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DataMap implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<String, InputStream> data;

    DataMap(Map<String, InputStream> data) {
        this.data = data;
    }

    public Map<String, InputStream> getData() {
        return data;
    }

    public static DataMap lookup(JsonNode data) throws IOException {
        var result = new HashMap<String, InputStream>();
        Iterator<Map.Entry<String, JsonNode>> properties = data.fields();
        while (properties.hasNext()) {
            Map.Entry<String, JsonNode> prop = properties.next();
            result.put(prop.getKey(), new ByteArrayInputStream(MAPPER.writeValueAsString(prop.getValue()).getBytes(UTF_8)));
        }
        return new DataMap(result);
    }

    @Override
    public void close() throws IOException {
        IOException lastException = null;
        for (var entry : data.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                lastException = e;
            }
        }
        if (lastException != null) throw lastException;
    }

}
