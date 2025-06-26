package eu.ai4eosc.provenance.api.utils;

import eu.ai4eosc.provenance.api.tables.rmlbuilder.RMLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ISReader {
    static final Logger log = LoggerFactory.getLogger(ISReader.class);

    public String readResourceFileAsString(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) return null;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    public List<String> readResourceFileAsStringList(String path) throws IOException {
        log.info("reading {}", path);
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return new ArrayList<>(reader.lines().toList());
        }
    }
}
