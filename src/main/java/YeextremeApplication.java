import io.github.ludovicianul.yeextreme.bulb.YeelightCommunicator;
import io.github.ludovicianul.yeextreme.ci.BuildExtractorMapping;
import io.github.ludovicianul.yeextreme.ci.BuildStatus;
import io.github.ludovicianul.yeextreme.config.PropertiesHolder;
import io.github.ludovicianul.yeextreme.config.ReservedColor;
import io.github.ludovicianul.yeextreme.config.RestClientProvider;
import io.github.ludovicianul.yeextreme.config.Telnet;
import io.github.ludovicianul.yeextreme.model.Color;
import io.github.ludovicianul.yeextreme.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YeextremeApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(YeextremeApplication.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String MAX_CONN_ATTEMPTS_PROP = "maxYeeLightConnectionAttempts";
    private static final String TELNET_PORT_PROP = "telnetPort";
    private static final String SECONDS_TO_PAUSE_CHECKING = "secondsBetweenChecks";

    private static final String MAX_CONN_ATTEMPTS = "10";
    private static final String DEFAULT_TELNET_PORT = "8888";
    private static final String MILLIS_TO_PAUSE_CHECLING = "15";

    public static void main(String[] args) throws Exception {
        String propsLocation = null;
        if (args != null && args.length >= 1) {
            propsLocation = args[0];
        }
        reloadProps(propsLocation);
        startTelnet();
        startMonitoring();
    }

    private static void reloadProps(String location) throws Exception {
        PropertiesHolder.reloadProperties(location);
    }


    private static void startMonitoring() throws Exception {
        int secondsToPause = Integer.parseInt(PropertiesHolder.getOtherProperties().getProperty(SECONDS_TO_PAUSE_CHECKING, MILLIS_TO_PAUSE_CHECLING)) * 1000;
        LOGGER.info("millis to pause between checks {}", secondsToPause);

        while (true) {
            LocalTime now = LocalTime.now();
            PropertiesHolder.bestCandidate = PropertiesHolder.getConfiguredTasks()
                    .stream().filter(task -> now.isBefore(task.getEnd()) && now.isAfter(task.getStart()))
                    .findFirst();
            LOGGER.info("best task candidate {}", PropertiesHolder.bestCandidate);
            if (PropertiesHolder.bestCandidate.isPresent()) {
                handleTask(PropertiesHolder.bestCandidate.get());
            }
            Thread.sleep(secondsToPause);
        }
    }

    private static void handleTask(Task task) throws Exception {
        String colorName = task.getColorName();
        if (task.isUrlTask()) {
            BuildExtractorMapping mapping = BuildExtractorMapping.fromCiServerName(task.getCiServer());
            if (mapping != null) {
                BuildStatus status = mapping.getBuildInfoExtractor().getBuildStatus(task.getUrl());
                LOGGER.info("build status {}", status);
                colorName = status.getColorName();
            }
        }

        colorName = checkIfAlwaysTaskOverrridesBestCandidate(colorName);

        sendColorToBulb(colorName);
    }

    private static String checkIfAlwaysTaskOverrridesBestCandidate(String colorName) {
        if (PropertiesHolder.alwaysTask != null) {
            String[] details = PropertiesHolder.alwaysTask.split(",");
            try {
                RestTemplate template = RestClientProvider.INSTANCE;
                template.headForHeaders(new URI(details[1]));
            } catch (Exception e) {
                LOGGER.warn("provided always task [{}] is unavailable. overwriting color previous color [{}] with [{}]",
                        PropertiesHolder.alwaysTask, colorName, details[0], e);
                colorName = details[0];
            }
        }
        return colorName;
    }

    private static void sendColorToBulb(String colorName) throws Exception {
        Color colorToSendToYeelight = PropertiesHolder.getConfiguredColors().stream()
                .filter(color -> color.getName().equalsIgnoreCase(colorName)).findFirst().orElse(ReservedColor.fromString(colorName).getColor());

        LOGGER.info("the color to be sent to yeelight {}", colorToSendToYeelight);

        attemptConnection(colorToSendToYeelight);
    }

    private static void attemptConnection(Color colorToSendToYeelight) throws InterruptedException {
        int connectionAttempts = 1;
        int maxAttempts = Integer.parseInt(PropertiesHolder.getOtherProperties().getProperty(MAX_CONN_ATTEMPTS_PROP, MAX_CONN_ATTEMPTS));
        while (connectionAttempts < maxAttempts) {
            try {
                LOGGER.info("attempt {} to communicate with Yeelight", connectionAttempts);
                YeelightCommunicator.sendColorToDevice(colorToSendToYeelight);
                break;
            } catch (Exception e) {
                LOGGER.error("there was an error while sending the color to yeelight", e);
                connectionAttempts++;
                Thread.sleep(1000);
            }
        }
    }

    private static void startTelnet() {
        int telnetPort = Integer.parseInt(PropertiesHolder.getOtherProperties().getProperty(TELNET_PORT_PROP, DEFAULT_TELNET_PORT));
        LOGGER.info("start listening on port {}", telnetPort);
        new Thread(() -> {
            try (ServerSocket srv = new ServerSocket(telnetPort)) {
                while (true) {
                    Socket skt = srv.accept();
                    executor.execute(new Telnet(skt));
                }
            } catch (Exception e) {
                LOGGER.error("Error starting telnet listener!", e);
            }

        }).start();
    }
}
