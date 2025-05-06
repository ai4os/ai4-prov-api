package eu.ai4eosc.provenance.api.fetcher.fetchers.dfo.jenkins;

import java.net.URL;
import java.util.List;

public record Changeset(Id jenkins,
                        String group,
                        String repo,
                        String commit,
                        String comment,
                        String authorName,
                        URL authorURL,
                        String authorEmail,
                        List<String> paths,
                        Long timeStamp) {};
