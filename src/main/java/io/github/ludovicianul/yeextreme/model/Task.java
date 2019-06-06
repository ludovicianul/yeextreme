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
        Task task = new Task();

        String[] allDetails = input.trim().split("=");
        if (allDetails.length != 2) {
            LOGGER.error("invalid task [{}] configuration", input);
            return null;
        }
        String[] data = allDetails[1].trim().split(",");
        if (data.length != 3) {
            LOGGER.error("invalid task [{}] syntax", input);
            return null;
        }
        String[] nameAndCi = allDetails[0].split("_");
        if (nameAndCi.length < 2) {
            LOGGER.error("invalid task naming [{}]", allDetails[0]);
            return null;
        }
        if (data[2].startsWith("http") && nameAndCi.length == 3) {
            task.name = nameAndCi[2];
            task.ciServer = nameAndCi[1];
        } else if (data[2].startsWith("http")) {
            LOGGER.error("you must specify a ci server for task [{}]", input);
            return null;
        } else {
            task.name = nameAndCi[1];
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        task.start = LocalTime.parse(data[0], formatter);
        task.end = LocalTime.parse(data[1], formatter);

        if (task.start.isAfter(task.end)) {
            LOGGER.error("invalid task [{}] time configuration", input);
        }

        if (data[2].startsWith("http")) {
            task.url = data[2];
            LOGGER.info("found an url task [{}]", input);
        } else {
            LOGGER.info("found a simple task [{}]", input);
            task.colorName = data[2];
        }
        return task;
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

    public String getName() {
        return name;
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
