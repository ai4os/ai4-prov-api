package eu.ai4eosc.provenance.api.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {
    static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    @Autowired
    private ClientSecrets clientSecrets;

    public Authentication getAuthentication(HttpServletRequest request) {
        String authToken = clientSecrets.apikey();
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(authToken)) {
            throw new BadCredentialsException("Invalid API Key");
        }
        return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}
