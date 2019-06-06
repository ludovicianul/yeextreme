package io.github.ludovicianul.yeextreme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Color {
    private static final Logger LOGGER = LoggerFactory.getLogger(Color.class);

    private String name;
    private int r;
    private int g;
    private int b;
    private int brightness;
    private boolean pulse;

    /**
     * Complete format for a color entry is: c_name=int,int,int,int,boolean
     *
     * @param input
     * @return
     */
    public static Color fromString(String input) {
        String[] allDetails = input.trim().split("=");
        if (allDetails.length != 2) {
            LOGGER.error("invalid color configuration [{}]", input);
            return null;
        }
        String[] details = allDetails[1].trim().split(",");
        if (details.length > 5 || details.length < 3) {
            LOGGER.info("invalid color syntax [{}]", input);
            return null;
        }
        Color color = new Color();
        color.name = allDetails[0];
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
    }

    public Color() {

    }

    public Color(String name, int r, int g, int b, int brightness, boolean pulse) {
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
        this.brightness = brightness;
        this.pulse = pulse;
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
