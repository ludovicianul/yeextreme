package io.github.ludovicianul.yeextreme.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ludovicianul.yeextreme.bulb.YeelightCommunicator;
import io.github.ludovicianul.yeextreme.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Telnet implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Telnet.class);

    private final Socket skt;

    public Telnet(Socket srv) {
        this.skt = srv;
    }

    public void run() {
        LOG.info("new connection");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
             PrintWriter pw = new PrintWriter(skt.getOutputStream(), true)) {
            pw.println("yes, master: ");

            String input;

            while (!skt.isClosed() && (input = br.readLine()) != null) {
                String received = this.sanitizeInput(input);
                String[] commands = this.getCommands(received);

                if (this.weHaveCommands(commands)) {
                    switch (commands[0]) {
                        case "quit":
                            this.closeConnection();
                            break;
                        case "ping":
                            this.healthy(pw);
                            break;
                        case "reloadProps":
                            this.reloadProps(pw, commands[1]);
                            break;
                        case "state":
                            this.getState(pw);
                            break;
                        case "colors":
                            this.printAvailableColors(pw);
                            break;
                        default:
                            this.notSupported(pw, commands[0]);
                    }
                }
            }

            LOG.info("session closed!");

        } catch (Exception e) {
            LOG.error("reply to master failed!", e);
        }
    }

    private void notSupported(PrintWriter pw, String command) {
        pw.println(command + " not supported!");
    }

    private void reloadProps(PrintWriter pw, String location) {
        try {
            PropertiesHolder.reloadProperties(location);
            YeelightCommunicator.reloadYeelight();
            pw.println("reload successful!");
        } catch (Exception e) {
            pw.println("error: " + e.getMessage());
        }
    }

    private void printAvailableColors(PrintWriter pw) {
        pw.println(PropertiesHolder.getConfiguredColors());
        LOG.info("colors sent over telnet connection");
    }

    private void healthy(PrintWriter pw) {
        LOG.info("pong");
        pw.println("pong");
    }

    private void closeConnection() throws IOException {
        skt.close();
    }

    private boolean weHaveCommands(String[] commands) {
        return commands != null && commands.length > 0;
    }

    private String[] getCommands(String input) {
        return input.split(" ");
    }

    private String sanitizeInput(String input) {
        if (input != null && input.contains("\b")) {
            input = input.replaceAll("^\b+|[^\b]\b", "");
        }

        return input != null ? input : "";
    }

    private void getState(PrintWriter pw) {
        LOG.info("state requested");
        final ObjectMapper mapper = new ObjectMapper();
        try {
            pw.println(mapper.writeValueAsString(PropertiesHolder.bestCandidate.orElseGet(Task::empty)));
        } catch (Exception e) {
            pw.println("error when getting state!");
            LOG.warn("error when getting state!", e);
        }
    }
}
