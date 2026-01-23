package com.example.radiusapi.utils;

import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.util.concurrent.TimeUnit;

public class TcpdumpWrapper {
    private final String tcpdumpCommand;
    private final int captureDuration;

    public TcpdumpWrapper(String tcpdumpCommand, int captureDuration) {
        this.tcpdumpCommand = tcpdumpCommand;
        this.captureDuration = captureDuration;
    }

    public String capturePackets() throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder(tcpdumpCommand.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        process.waitFor(captureDuration, TimeUnit.SECONDS);
        process.destroy();
        return output.toString();
    }



}
