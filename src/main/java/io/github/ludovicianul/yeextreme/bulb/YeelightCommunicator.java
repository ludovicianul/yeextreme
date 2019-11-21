package io.github.ludovicianul.yeextreme.bulb;

import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.enumeration.YeelightFlowAction;
import com.mollin.yapi.flow.YeelightFlow;
import com.mollin.yapi.flow.transition.YeelightColorTransition;
import com.mollin.yapi.flow.transition.YeelightSleepTransition;
import io.github.ludovicianul.yeextreme.config.PropertiesHolder;
import io.github.ludovicianul.yeextreme.model.Color;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures the communication with the Yeelight bulb
 */
public class YeelightCommunicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(YeelightCommunicator.class);
    private static final String YEELIGHT_IP = "yeelightIP";
    private static final String YEELIGHT_PORT = "yeelightPort";
    private static YeelightDevice device;

    static {
        reloadYeelight();
    }

    public static void reloadYeelight() {
        String ipAddress = PropertiesHolder.getOtherProperties().getProperty(YEELIGHT_IP);
        String port = PropertiesHolder.getOtherProperties().getProperty(YEELIGHT_PORT, "55443");

        if (StringUtils.isEmpty(ipAddress)) {
            throw new IllegalArgumentException("yeelight IP address must be defined!!!");
        }
        try {
            device = new YeelightDevice(ipAddress, Integer.parseInt(port));
        } catch (Exception e) {
            LOGGER.error("Exception while communication with the light bulb!", e);
        }
    }

    public static void sendColorToDevice(Color color) throws RuntimeException {
        try {
            device.stopFlow();
            device.setPower(true);
            device.setRGB(color.getR(), color.getG(), color.getB());
            device.setBrightness(color.getBrightness());
            if (color.isPulse()) {
                startFlow(color.getR(), color.getG(), color.getB());
            }
            LOGGER.info("color {} sent successfully to the device {}", color, PropertiesHolder.getOtherProperties().getProperty(YEELIGHT_IP));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This simulates a pulse flow transitioning from the supplied color to gray and back with 1 seconds delay
     *
     * @param r red
     * @param g green
     * @param b blue
     * @throws Exception in case something goes wrong while communicating with the Yeelight Device
     */
    private static void startFlow(int r, int g, int b) throws Exception {
        YeelightFlow yeelightFlow = new YeelightFlow(YeelightFlow.INFINITE_COUNT, YeelightFlowAction.RECOVER);
        YeelightColorTransition c1 = new YeelightColorTransition(r, g, b, 1000);
        YeelightSleepTransition sleep = new YeelightSleepTransition(1000);
        YeelightColorTransition c2 = new YeelightColorTransition(125, 125, 125, 1000);
        yeelightFlow.getTransitions().add(c1);
        yeelightFlow.getTransitions().add(sleep);
        yeelightFlow.getTransitions().add(c2);

        device.startFlow(yeelightFlow);
    }
}
