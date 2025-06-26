package eu.ai4eosc.provenance.api.controllers.schemas.input.credentials;

public record MLFlowCredentials(String username, String password) implements ICredentials {
}
