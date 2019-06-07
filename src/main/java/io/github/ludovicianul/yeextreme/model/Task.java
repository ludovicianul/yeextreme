package io.github.ludovicianul.yeextreme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Task implements Comparable<Task> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private String name;
    private LocalTime start;
    private LocalTime end;
    private String colorName;
    private String url;
    private String ciServer;

    public static Task empty() {
        Task task = new Task();
        task.name = "no tasks";
        return task;
    }

    public static Task fromString(String input) {
        try {
            Task task = new Task();
            LOGGER.info("parsing [{}]", input);
            String[] allDetails = splitLine(input);
            String[] data = splitProperty(allDetails[1]);
            String[] nameAndCi = getTaskNameAndCi(allDetails[0]);

            populateTaskNameAndCiServer(task, data[2], nameAndCi);
            populateStartAndEnd(task, data);
            populateColorNameOrUrl(task, data[2]);

            return task;
        } catch (Exception e) {
            LOGGER.error("something went wrong while parsing {}", input);
        }
        return null;
    }

    private static void populateColorNameOrUrl(Task task, String datum) {
        if (datum.startsWith("http")) {
            task.url = datum;
            LOGGER.info("found an url task");
        } else {
            LOGGER.info("found a simple task");
            task.colorName = datum.toUpperCase();
        }
    }

    private static void populateStartAndEnd(Task task, String[] data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        task.start = LocalTime.parse(data[0], formatter);
        task.end = LocalTime.parse(data[1], formatter);

        if (task.start.isAfter(task.end)) {
            throw new IllegalArgumentException("invalid task time configuration");
        }
    }

    private static void populateTaskNameAndCiServer(Task task, String datum, String[] nameAndCi) {
        if (datum.startsWith("http") && nameAndCi.length == 3) {
            task.name = nameAndCi[2].toUpperCase();
            task.ciServer = nameAndCi[1];
        } else if (datum.startsWith("http")) {
            throw new IllegalArgumentException("you must specify a ci server for task");
        } else {
            task.name = nameAndCi[1].toUpperCase();
        }
    }

    private static String[] getTaskNameAndCi(String allDetail) {
        String[] nameAndCi = allDetail.split("_");
        if (nameAndCi.length < 2) {
            throw new IllegalArgumentException("invalid task naming");
        }
        return nameAndCi;
    }

    private static String[] splitProperty(String allDetail) {
        String[] data = allDetail.trim().split(",");
        if (data.length != 3) {
            throw new IllegalArgumentException("invalid task syntax");
        }
        return data;
    }

    private static String[] splitLine(String input) {
        String[] allDetails = input.trim().split("=");
        if (allDetails.length != 2) {
            throw new IllegalArgumentException("invalid task configuration");
        }
        return allDetails;
    }


    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", colorName='" + colorName + '\'' +
                ", url='" + url + '\'' +
                ", ciServer='" + ciServer + '\'' +
                '}';
    }

    @Override
    public int compareTo(Task o) {
        return (int) (this.end.toSecondOfDay() - this.start.toNanoOfDay() - (o.end.toSecondOfDay() - o.start.toSecondOfDay()));
    }

    public boolean isUrlTask() {
        return this.url != null;
    }

    public String getCiServer() {
        return ciServer;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public String getColorName() {
        return colorName;
    }

    public String getUrl() {
        return url;
    }
}
