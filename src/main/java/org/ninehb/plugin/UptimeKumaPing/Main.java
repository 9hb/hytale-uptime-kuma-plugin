package org.ninehb.plugin.UptimeKumaPing;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static Main instance;
    private ScheduledExecutorService scheduler;
    private UptimeConfig config;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("UptimeKuma plugin is setting up...");

        this.config = UptimeConfig.load(getDataDirectory());

        if (config.domain == null || config.domain.isEmpty() ||
                config.pushToken == null || config.pushToken.isEmpty()) {
            LOGGER.atWarning().log("Config created but not set! Please edit uptime_config.json in plugin data folder");
            return;
        }

        LOGGER.atInfo().log("UptimeKuma setup complete!");
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("UptimeKuma plugin starting...");

        if (config != null && config.domain != null && !config.domain.isEmpty()) {
            startPinging();
        }

        LOGGER.atInfo().log("UptimeKuma plugin started!");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("UptimeKuma plugin shutting down...");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }

        instance = null;
        LOGGER.atInfo().log("UptimeKuma plugin stopped!");
    }

    private void startPinging() {
        scheduler = Executors.newScheduledThreadPool(1);
        long interval = config.intervalSeconds > 0 ? config.intervalSeconds : 60;
        LOGGER.atInfo().log("Pinging " + config.domain + " every " + interval + "s");

        scheduler.scheduleAtFixedRate(this::sendPing, 0, interval, TimeUnit.SECONDS);
    }

    private void sendPing() {
        try {
            String cleanDomain = config.domain.replace("http://", "").replace("https://", "");
            if (cleanDomain.endsWith("/")) {
                cleanDomain = cleanDomain.substring(0, cleanDomain.length() - 1);
            }

            String encodedMsg = java.net.URLEncoder.encode(config.message, java.nio.charset.StandardCharsets.UTF_8);
            String pingParam = "";
            if (config.duration != null && !config.duration.isEmpty()) {
                pingParam = "&ping="
                        + java.net.URLEncoder.encode(config.duration, java.nio.charset.StandardCharsets.UTF_8);
            }

            String urlString = String.format("https://%s/api/push/%s?status=up&msg=%s%s",
                    cleanDomain, config.pushToken, encodedMsg, pingParam);

            java.net.URL url = java.net.URI.create(urlString).toURL();
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int code = con.getResponseCode();
            if (code != 200) {
                LOGGER.atWarning().log("Ping failed with code: {}", code);
            }
            con.disconnect();

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Ping error occurred");
        }
    }

    public static Main getInstance() {
        return instance;
    }
}