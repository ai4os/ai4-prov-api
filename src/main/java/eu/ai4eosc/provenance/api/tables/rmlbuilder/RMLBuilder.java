package eu.ai4eosc.provenance.api.tables.rmlbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.ai4eosc.provenance.api.tables.rmlbuilder.bridgesDTO.Bridge;
import eu.ai4eosc.provenance.api.tables.rmlbuilder.bridgesDTO.Bridges;
import eu.ai4eosc.provenance.api.utils.ISReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds an RML Fragments base on RML Generator FileSystem
 * It's used at the beginning of the service execution to generate
 * all the potential permutations of metadata sources...
 */
public class RMLBuilder {
    static final Logger log = LoggerFactory.getLogger(RMLBuilder.class);
    static final String originFile = "/origin";
    static final String contentFile = "/content.ttl";
    static final String bridgesSubDir = "/bridges";
    static final String baseBridgeFile = "/base.yaml";
    static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    ISReader isReader = new ISReader();

    public RMLBuilder() {
    }

    public RMLStruct buildSingleRML(String id) {
        String path = "/db-init/types/other/" + id + contentFile;
        return new RMLStruct(null, isReader.readResourceFileAsString(path));
    }

    public RMLStruct buildProvenanceRML(String id, List<String> extensions) throws IOException {
        log.info("Building Provenance RML -> {} with extensions {}", id, extensions);
        ObjectNode origins = JsonNodeFactory.instance.objectNode();
        String treePath = buildRMLTreePath(id);
        newOriginPair(origins, treePath + originFile);

        log.info("reading core RML content with id {}  ... ", id);
        // Load CORE RML
        String coreRMLPath = treePath + contentFile;
        List<String> rmlLines = isReader.readResourceFileAsStringList(coreRMLPath);

        log.info("coreRML reading complete");
        log.info("Starting including extensions origins...");
        List<String> extensionsPath = extensions.stream()
                .map(e -> buildExtensionPath(treePath, e)).toList();
        // Add origins
        for (String extP : extensionsPath) {
            newOriginPair(origins, extP + originFile); // add the extension origin to origins
        }
        log.info("Extensions origins included");

        // Add BASE BRIDGEs TO CORE
        // Read all baseBridges
        // Then add them in descendant indexes order
        log.info("adding base bridges to core...");
        List<String> baseBridgePaths = extensionsPath.stream().map(s -> s + bridgesSubDir + baseBridgeFile).toList();
        List<Bridge> descendantBridges = getAllBridgesDescendantOrder(baseBridgePaths);

        for (Bridge bridge : descendantBridges) {
            List<String> bridgeContent = Arrays.asList(bridge.bridge().split("\\R"));
            rmlLines.addAll(bridge.index(), bridgeContent);
        }

        rmlLines.add("\n");
        log.info("base bridges insertion to core completed");
        // Add conditional bridges for each extension
        log.info("Loading bridges between extensions...");
        for (String extension : extensions) {
            List<String> externalExtensions = extensions.stream().filter(e -> !e.equals(extension)).toList();
            List<String> completeExtensionRML = buildExtensionRML(buildExtensionPath(treePath, extension), externalExtensions);
            log.info("Extension {} , complete extension: {}", extension, completeExtensionRML.getFirst());
            rmlLines.addAll(completeExtensionRML);
        }

        String completeRML = String.join("\n", rmlLines);

        // Iterate through extensions
        return new RMLStruct(origins, completeRML);
    }

    String buildRMLTreePath(String endPath) {
        return "/db-init/types/trees/" + endPath;
    }

    String buildExtensionPath(String treePath, String extension) {
        return treePath + "/extensions/" + extension;
    }

    void newOriginPair(ObjectNode objectNode, String pathToOriginFile
    ) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(pathToOriginFile);) {
            if (is == null) return;
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            while (reader.ready()) {
                String originPair = reader.readLine();
                String[] keyValue = originPair.split(":");
                ObjectNode idObjectNode = JsonNodeFactory.instance.objectNode();
                idObjectNode.put("id", keyValue[1]);
                objectNode.set(keyValue[0], idObjectNode);
            }
        }
    }

    List<Bridge> getAllBridgesDescendantOrder(List<String> bridgesPaths) throws IOException {
        log.info("accesing bridges: {}", bridgesPaths);
        List<Bridge> allBridges = new ArrayList<Bridge>();
        for (String bridgePath : bridgesPaths) {
            log.info("trying to access: {}", bridgePath);
            try (InputStream bridgeIS = getClass().getResourceAsStream(bridgePath)) {
                if (bridgeIS != null) {
                    Bridges bridges = yamlMapper.readValue(bridgeIS, Bridges.class);
                    if (bridges.bridges() != null && !bridges.bridges().isEmpty())
                        allBridges.addAll(bridges.bridges());
                } else {
                    log.info("bridgePath: {} does not exist", bridgePath);
                }
            }
        }
        // Now order em by descendant order
        allBridges.sort((a, b) -> Integer.compare(b.index(), a.index()));
        return allBridges;
    }

    /**
     * Build the complete RML of a extension,
     * including those extensions with defined bridges
     *
     * @param targetExtensionPath
     * @param potentialBridges
     * @return
     */
    List<String> buildExtensionRML(String targetExtensionPath, List<String> potentialBridges) throws IOException {
        log.info("Building extension {} for potential bridges: {}", targetExtensionPath, potentialBridges);
        // Get all the bridges for an extension (excluding base.yaml)
        List<String> potentialBridgesPaths = potentialBridges.stream().map(pb -> targetExtensionPath + bridgesSubDir + "/" + pb + ".yaml").toList();
        List<Bridge> descendantBridges = getAllBridgesDescendantOrder(potentialBridgesPaths);

        // Load base extension and writes in it the extensions bridges ordered by descendant index order
        List<String> baseExtensionRML = isReader.readResourceFileAsStringList(targetExtensionPath + contentFile);
        for (Bridge bridge : descendantBridges) {
            List<String> bridgeContent = Arrays.asList(bridge.bridge().split("\\R"));
            baseExtensionRML.addAll(bridge.index(), bridgeContent);
        }
        return baseExtensionRML;
    }
}
