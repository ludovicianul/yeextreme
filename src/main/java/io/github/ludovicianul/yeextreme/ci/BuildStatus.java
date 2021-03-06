package io.github.ludovicianul.yeextreme.ci;

public enum BuildStatus {
    SUCCESS("green"), FAILED("red"), BUILDING("blue"), UNSTABLE("yellow"), ERROR("all");

    private final String color;

    BuildStatus(String color) {
        this.color = color;
    }

    public String getColorName() {
        return this.color;
    }
}
