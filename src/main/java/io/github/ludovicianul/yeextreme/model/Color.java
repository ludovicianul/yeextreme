package io.github.ludovicianul.yeextreme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Color {
    private static final Logger LOGGER = LoggerFactory.getLogger(Color.class);

    private String name;
    private int r;
    private int g;
    private int b;
    private int brightness;
    private boolean pulse;

    private Color() {
    }

    public Color(String name, int r, int g, int b, int brightness, boolean pulse) {
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
        this.brightness = brightness;
        this.pulse = pulse;
    }

    /**
     * Complete format for a color entry is: c_name=int,int,int,int,boolean
     *
     * @param input line from the property file
     * @return a valid color is the provided line complies with the syntax or null otherwise
     */
    public static Color fromString(String input) {
        try {
            LOGGER.info("parsing color line [{}]", input);
            String[] allDetails = splitLine(input);
            String[] details = splitProperty(allDetails[1]);

            Color color = new Color();
            color.name = allDetails[0].toUpperCase().replaceAll("C_", "");
            color.r = Integer.parseInt(details[0]);
            color.g = Integer.parseInt(details[1]);
            color.b = Integer.parseInt(details[2]);
            color.brightness = 100;
            color.pulse = false;
            if (details.length >= 4) {
                color.brightness = Integer.parseInt(details[3]);
            }
            if (details.length >= 5) {
                color.pulse = Boolean.parseBoolean(details[4]);
            }
            return color;
        } catch (Exception e) {
            LOGGER.error("something went wrong while parsing color [{}]", input);
        }
        return null;
    }

    private static String[] splitProperty(String allDetail) {
        String[] details = allDetail.trim().split(",");
        if (details.length > 5 || details.length < 3) {
            throw new IllegalArgumentException("invalid color syntax");
        }
        return details;
    }

    private static String[] splitLine(String input) {
        String[] allDetails = input.trim().split("=");
        if (allDetails.length != 2) {
            throw new IllegalArgumentException("invalid color configuration");
        }
        return allDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return r == color.r &&
                g == color.g &&
                b == color.b &&
                brightness == color.brightness &&
                pulse == color.pulse &&
                name.equals(color.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, r, g, b, brightness, pulse);
    }

    public String getName() {
        return name;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getBrightness() {
        return brightness;
    }

    public boolean isPulse() {
        return pulse;
    }

    @Override
    public String toString() {
        return "Color{" +
                "name='" + name + '\'' +
                ", r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", brightness=" + brightness +
                ", pulse=" + pulse +
                '}';
    }
}
