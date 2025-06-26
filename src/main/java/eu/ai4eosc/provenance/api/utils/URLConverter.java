package eu.ai4eosc.provenance.api.utils;

import org.springframework.util.ObjectUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class URLConverter {
    public static Optional<URL> string2url(String url) throws MalformedURLException, URISyntaxException {
        if (!ObjectUtils.isEmpty(url)) {
            return Optional.of(new URI(url).toURL());
        }
        return Optional.empty();
    }
}
