package io.github.ludovicianul.yeextreme.ci;

public enum BuildStatus {
    SUCCESS("green"), FAILED("red"), BUILDING("blue");

    private String color;

    BuildStatus(String color) {
        this.color = color;
    }

    public String getColorName() {
        return this.color;
    }
}
