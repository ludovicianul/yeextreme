package io.github.ludovicianul.yeextreme.ci;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.ludovicianul.yeextreme.config.RestClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class JenkinsBuildExtractor implements BuildInfoExtractor {
    private static final String BUILDING = "building";
    private static final String SUCCESS = "success";
    private static final String UNSTABLE = "unstable";

    private static final String RESULT = "result";
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsBuildExtractor.class);


    @Override
    public BuildStatus getBuildStatus(String url) {
        try {
            RestTemplate template = RestClientProvider.INSTANCE;
            String resultObject = template.getForObject(url, String.class);
            LOGGER.debug("jenkins result {}", resultObject);

            JsonObject result = new Gson().fromJson(resultObject, JsonObject.class);
            if (result.get(BUILDING) != null && result.get(BUILDING).getAsString().equalsIgnoreCase("true")) {
                return BuildStatus.BUILDING;
            } else if (result.get(RESULT).getAsString().toLowerCase().contains(SUCCESS)) {
                return BuildStatus.SUCCESS;
            } else if (result.get(RESULT).getAsString().toLowerCase().contains(UNSTABLE)) {
                return BuildStatus.UNSTABLE;
            }
            return BuildStatus.FAILED;
        } catch (Exception e) {
            LOGGER.error("Something went wrong while getting build status", e);
            return BuildStatus.ERROR;
        }
    }
}
