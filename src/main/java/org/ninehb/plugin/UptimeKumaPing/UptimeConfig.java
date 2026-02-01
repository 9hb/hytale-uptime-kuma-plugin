package org.ninehb.plugin.UptimeKumaPing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class UptimeConfig {
    public String domain = "";
    public String pushToken = "";
    public int intervalSeconds = 600;
    public String message = "OK";
    public String duration = "";

    public static UptimeConfig load(Path dataDirectory) {
        UptimeConfig config = new UptimeConfig();

        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            Main.LOGGER.atSevere().withCause(e).log("Could not create data directory");
        }

        File configFile = dataDirectory.resolve("uptime_config.json").toFile();

        if (!configFile.exists()) {
            save(config, configFile);
            return config;
        }

        try {
            for (String line : Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.startsWith("\"domain\":")) {
                    config.domain = extractValue(line);
                } else if (line.startsWith("\"pushToken\":")) {
                    config.pushToken = extractValue(line);
                } else if (line.startsWith("\"message\":")) {
                    config.message = extractValue(line);
                } else if (line.startsWith("\"duration\":")) {
                    config.duration = extractValue(line);
                } else if (line.startsWith("\"intervalSeconds\":")) {
                    String val = line.split(":")[1].replace(",", "").trim();
                    try {
                        config.intervalSeconds = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        config.intervalSeconds = 600;
                    }
                }
            }
        } catch (IOException e) {
            Main.LOGGER.atSevere().withCause(e).log("Failed to load config file");
        }
        return config;
    }

    private static String extractValue(String line) {
        try {
            int firstQuote = line.indexOf("\"", line.indexOf(":"));
            int lastQuote = line.lastIndexOf("\"");
            if (firstQuote != -1 && lastQuote != -1 && lastQuote > firstQuote) {
                return line.substring(firstQuote + 1, lastQuote);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public static void save(UptimeConfig config, File configFile) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"domain\": \"").append(config.domain != null ? config.domain : "").append("\",\n");
        json.append("  \"pushToken\": \"").append(config.pushToken != null ? config.pushToken : "").append("\",\n");
        json.append("  \"intervalSeconds\": ").append(config.intervalSeconds).append(",\n");
        json.append("  \"message\": \"").append(config.message != null ? config.message : "OK").append("\",\n");
        json.append("  \"duration\": \"").append(config.duration != null ? config.duration : "").append("\"\n");
        json.append("}\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile, StandardCharsets.UTF_8))) {
            writer.write(json.toString());
            Main.LOGGER.atInfo().log("Config saved to: {}", configFile.getAbsolutePath());
        } catch (IOException e) {
            Main.LOGGER.atSevere().withCause(e).log("Failed to save config file");
        }
    }
}