package io.github.ludovicianul.yeextreme.config;

import io.github.ludovicianul.yeextreme.model.Color;
import io.github.ludovicianul.yeextreme.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesHolder {
    private static final String DEFAULT_CONFIG = "yeextreme.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesHolder.class);

    private static List<Color> configuredColors;
    private static List<Task> configuredTasks;
    private static Properties otherProperties;

    public static Optional<Task> bestCandidate;

    public static Task alwaysTask;

    public static void reloadProperties(String location) throws URISyntaxException, IOException {
        LOGGER.info("reloading properties from {}", location);
        Path path = Paths.get(DEFAULT_CONFIG);
        if (StringUtils.isNotEmpty(location) && location.startsWith("http")) {
            path = Paths.get(new URI(location));
        } else if (StringUtils.isNotEmpty(location)) {
            path = Paths.get(location);
        }

        List<String> configLines = Files.readAllLines(path);
        parseColor(configLines);
        parseTasks(configLines);
        parseOtherProperties(configLines);
        parseAlwaysTask(configLines);

        LOGGER.warn("if you don't find all the supplied tasks and colors in the above lists it means they were ignored. remember to start each task with 'task_' and each color with 'c_'");
    }

    private static void parseAlwaysTask(List<String> configLines) {
        Optional<String> alwaysString = configLines.stream().filter(line -> line.toLowerCase().equals("task_always")).findFirst();
        if (alwaysString.isPresent()) {
            alwaysTask = Task.fromString(alwaysString.get());
            if (alwaysTask != null && !alwaysTask.isUrlTask()) {
                LOGGER.error("task_always must be a value url [{}]. it will be ignored...", alwaysTask);
                alwaysTask = null;
            }
        }
    }

    private static void parseOtherProperties(List<String> configLines) {
        otherProperties = new Properties();

        configLines.stream().filter(line -> !line.startsWith("c_") && !line.startsWith("task_") && StringUtils.isNotEmpty(line) && !line.startsWith("#")).forEach(line -> {
            String[] parsed = line.split("=");
            otherProperties.put(parsed[0], parsed[1]);
        });

        LOGGER.info("Other properties {}", otherProperties);
    }


    /**
     * All color properties must start with C_
     *
     * @param lines all lines starting with C_
     */
    private static void parseColor(List<String> lines) {
        List<String> colors = lines.stream().filter(line -> line.toLowerCase().startsWith("c_")).collect(Collectors.toList());
        configuredColors = colors.stream().map(Color::fromString).filter(Objects::nonNull).collect(Collectors.toList());
        LOGGER.info("final colors list {}", configuredColors);
    }

    private static void parseTasks(List<String> lines) {
        List<String> tasks = lines.stream().filter(line -> line.toLowerCase().startsWith("task_")).collect(Collectors.toList());
        configuredTasks = tasks.stream().map(Task::fromString).filter(Objects::nonNull).collect(Collectors.toList());

        Collections.sort(configuredTasks);
        LOGGER.info("final tasks list {}", configuredTasks);
    }

    public static List<Color> getConfiguredColors() {
        return configuredColors;
    }

    public static List<Task> getConfiguredTasks() {
        return configuredTasks;
    }

    public static Properties getOtherProperties() {
        return otherProperties;
    }


}
