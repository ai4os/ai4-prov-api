package eu.ai4eosc.provenance.api.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@Component
public class AuthenticationFilter extends GenericFilterBean { // Should this be a bean inside SecurityConfig?
    static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    AuthenticationService authenticationService;

    private static final Set<String> PROTECTED_PATHS = Set.of("/meta-data");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String path = httpServletRequest.getRequestURI();
            if (PROTECTED_PATHS.contains(path)) {
                Authentication authentication = authenticationService.getAuthentication(httpServletRequest);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            log.error("[*] Authentication Filter Error: ", e);
            // Check to see what exception it is and if is Exception just return 500
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = httpResponse.getWriter();
            writer.print("Invalid API Key");
            writer.flush();
            writer.close();
        }
        catch(Exception e) {
            log.error("[*] Authentication Filter Error: ", e);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter writer = httpResponse.getWriter();
            writer.print("Internal Server Error");
            writer.flush();
            writer.close();
        }
    }
}
