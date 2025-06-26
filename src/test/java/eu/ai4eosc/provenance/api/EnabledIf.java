package eu.ai4eosc.provenance.api;

public class EnabledIf {

    static boolean runIntegration() {
        return "true".equalsIgnoreCase(System.getenv("RUN_INTEGRATION_TESTS"));
    }

}

