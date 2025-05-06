package eu.ai4eosc.provenance.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.ai4eosc.provenance.api.services.rdfgraph.IRINode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(IRINode.class, new IRINode.IRINodeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
