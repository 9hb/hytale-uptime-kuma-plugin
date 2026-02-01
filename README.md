# Uptime Kuma Push Plugin

A simple plugin for Hytale server that sends a periodic ping (using Push method) to your Uptime Kuma instance. This lets it know the server is up. If the ping doesn't arrive in time, it's gonna trigger a downtime alert.

## Setup

1. Drop the plugin into your server's plugins folder.
2. Configure required parameters in the config file
3. Restart your server _or_ reload the plugin using `/plugin reload org.ninehb.hytaleplugin:UptimeKumaPing`

## License

This project is licensed under the MIT License.
See the [LICENSE](LICENSE) file for details.
