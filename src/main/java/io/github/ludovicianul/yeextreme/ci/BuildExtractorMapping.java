package io.github.ludovicianul.yeextreme.ci;

/**
 * This holds the mapping between the ci server name and an actual extractor implementation.
 * If you want to add support for a new ci server, you must also add a new mapping here.
 */
public enum BuildExtractorMapping {
    JENKINS(new JenkinsBuildExtractor());

    private BuildInfoExtractor buildInfoExtractor;

    BuildExtractorMapping(BuildInfoExtractor infoExtractor) {
        this.buildInfoExtractor = infoExtractor;
    }

    public static BuildExtractorMapping fromCiServerName(String ciServerName) {
        for (BuildExtractorMapping mapping : values()) {
            if (mapping.name().toLowerCase().equals(ciServerName.toLowerCase())) {
                return mapping;
            }
        }

        return null;
    }

    public BuildInfoExtractor getBuildInfoExtractor() {
        return this.buildInfoExtractor;
    }
}
