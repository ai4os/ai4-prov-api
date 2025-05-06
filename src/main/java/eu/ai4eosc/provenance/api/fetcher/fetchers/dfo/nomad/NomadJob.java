package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.nomad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NomadJob {
        String origin;
        String applicationId;
        @JsonProperty("job_ID") String jobId;
        String name;
        String status;
        String owner;
        String title;
        String description;
        @JsonProperty("docker_image") String dockerImage;
        @JsonProperty("docker_command") String dockerCommand;
        @JsonProperty("submit_time") String submitTime;
        ResourcesDetails resources;
        EndpointsRegistry endpoints;
        @JsonProperty("active_endpoints") String activeEndpoints; // Maybe it's a map??
        @JsonProperty("main_endpoint") String mainEndpoint;
        @JsonProperty("alloc_ID") String allocId;
        String datacenter;
        @JsonProperty("alloc_start") String allocStart;
        @JsonProperty("alloc_end") String allocEnd;
        String namespace;

    record ResourcesDetails (
            @JsonProperty("cpu_num") Integer cpuNumber,
            @JsonProperty("cpu_MHz") Integer cpuFreq,
            @JsonProperty("gpu_num") Integer gpuNumber,
            @JsonProperty("memory_MB") Integer memoryMB,
            @JsonProperty("disk_MB") Integer diskMB
            ) {}

    record EndpointsRegistry (
            String api,
            String monitor,
            String ide,
            String custom
    ) {}
}
