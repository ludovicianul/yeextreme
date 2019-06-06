package io.github.ludovicianul.yeextreme.ci;

/**
 * Abstraction over different ci server build info extractor implementations
 */
public interface BuildInfoExtractor {

    BuildStatus getBuildStatus(String url);
}
