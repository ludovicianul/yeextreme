package io.github.ludovicianul.yeextreme.config;

import io.github.ludovicianul.yeextreme.model.Color;

public enum ReservedColor {
    RED(255, 0, 0, 80, false), GREEN(0, 255, 0, 80, false),
    BLUE(0, 0, 255, 80, true),
    WHITE(254, 254, 254, 80, false);

    private Color color;

    ReservedColor(int r, int g, int b, int brightness, boolean pulse) {
        color = new Color(this.name(), r, g, b, brightness, pulse);
    }

    public static ReservedColor fromString(String colorName) {
        for (ReservedColor reservedColor : values()) {
            if (reservedColor.name().equalsIgnoreCase(colorName)) {
                return reservedColor;
            }
        }

        return WHITE;
    }

    public Color getColor() {
        return color;
    }
}
