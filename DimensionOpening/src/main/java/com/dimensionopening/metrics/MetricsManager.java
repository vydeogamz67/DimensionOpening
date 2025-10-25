package com.dimensionopening.metrics;

import com.dimensionopening.DimensionOpeningPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsManager {
    private final DimensionOpeningPlugin plugin;
    private final Map<String, Integer> dimensionOpenCount;
    private final Map<String, Integer> dimensionCloseCount;
    private final Map<String, Integer> playerAccessAttempts;
    private final Map<String, Integer> playerAccessDenied;
    private final Map<String, Long> dimensionUptime;
    private final Map<String, Long> dimensionLastOpened;
    private final File metricsFile;
    
    public MetricsManager(DimensionOpeningPlugin plugin) {
        this.plugin = plugin;
        this.dimensionOpenCount = new ConcurrentHashMap<>();
        this.dimensionCloseCount = new ConcurrentHashMap<>();
        this.playerAccessAttempts = new ConcurrentHashMap<>();
        this.playerAccessDenied = new ConcurrentHashMap<>();
        this.dimensionUptime = new ConcurrentHashMap<>();
        this.dimensionLastOpened = new ConcurrentHashMap<>();
        
        // Create metrics directory and file
        File metricsDir = new File(plugin.getDataFolder(), "metrics");
        if (!metricsDir.exists()) {
            metricsDir.mkdirs();
        }
        this.metricsFile = new File(metricsDir, "statistics.txt");
        
        // Start periodic metrics saving
        startMetricsSaving();
    }
    
    /**
     * Record when a dimension is opened
     */
    public void recordDimensionOpen(String dimension) {
        dimensionOpenCount.merge(dimension, 1, Integer::sum);
        dimensionLastOpened.put(dimension, System.currentTimeMillis());
    }
    
    /**
     * Record when a dimension is closed
     */
    public void recordDimensionClose(String dimension) {
        dimensionCloseCount.merge(dimension, 1, Integer::sum);
        
        // Calculate uptime if dimension was previously opened
        Long openTime = dimensionLastOpened.get(dimension);
        if (openTime != null) {
            long uptime = System.currentTimeMillis() - openTime;
            dimensionUptime.merge(dimension, uptime, Long::sum);
        }
    }
    
    /**
     * Record when a player attempts to access a dimension
     */
    public void recordPlayerAccessAttempt(Player player, String dimension) {
        String key = player.getName() + ":" + dimension;
        playerAccessAttempts.merge(key, 1, Integer::sum);
    }
    
    /**
     * Record when a player is denied access to a dimension
     */
    public void recordPlayerAccessDenied(Player player, String dimension) {
        String key = player.getName() + ":" + dimension;
        playerAccessDenied.merge(key, 1, Integer::sum);
    }
    
    /**
     * Get dimension open statistics
     */
    public Map<String, Integer> getDimensionOpenStats() {
        return new HashMap<>(dimensionOpenCount);
    }
    
    /**
     * Get dimension close statistics
     */
    public Map<String, Integer> getDimensionCloseStats() {
        return new HashMap<>(dimensionCloseCount);
    }
    
    /**
     * Get player access attempt statistics
     */
    public Map<String, Integer> getPlayerAccessStats() {
        return new HashMap<>(playerAccessAttempts);
    }
    
    /**
     * Get player access denied statistics
     */
    public Map<String, Integer> getPlayerDeniedStats() {
        return new HashMap<>(playerAccessDenied);
    }
    
    /**
     * Get dimension uptime statistics (in milliseconds)
     */
    public Map<String, Long> getDimensionUptimeStats() {
        return new HashMap<>(dimensionUptime);
    }
    
    /**
     * Get formatted uptime for a dimension
     */
    public String getFormattedUptime(String dimension) {
        Long uptime = dimensionUptime.get(dimension);
        if (uptime == null) {
            return "No data";
        }
        
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Generate a comprehensive metrics report
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Dimension Opening Plugin Metrics ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        // Dimension open/close statistics
        report.append("Dimension Open Count:\n");
        for (Map.Entry<String, Integer> entry : dimensionOpenCount.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        report.append("\nDimension Close Count:\n");
        for (Map.Entry<String, Integer> entry : dimensionCloseCount.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Dimension uptime
        report.append("\nDimension Uptime:\n");
        for (Map.Entry<String, Long> entry : dimensionUptime.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(getFormattedUptime(entry.getKey())).append("\n");
        }
        
        // Player access statistics
        report.append("\nPlayer Access Attempts:\n");
        Map<String, Integer> playerTotals = new HashMap<>();
        for (Map.Entry<String, Integer> entry : playerAccessAttempts.entrySet()) {
            String player = entry.getKey().split(":")[0];
            playerTotals.merge(player, entry.getValue(), Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : playerTotals.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Access denied statistics
        report.append("\nPlayer Access Denied:\n");
        Map<String, Integer> deniedTotals = new HashMap<>();
        for (Map.Entry<String, Integer> entry : playerAccessDenied.entrySet()) {
            String player = entry.getKey().split(":")[0];
            deniedTotals.merge(player, entry.getValue(), Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : deniedTotals.entrySet()) {
            report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Save metrics to file
     */
    public void saveMetrics() {
        try (FileWriter writer = new FileWriter(metricsFile)) {
            writer.write(generateReport());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save metrics: " + e.getMessage());
        }
    }
    
    /**
     * Start periodic metrics saving
     */
    private void startMetricsSaving() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveMetrics();
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L); // Save every 5 minutes
    }
    
    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        dimensionOpenCount.clear();
        dimensionCloseCount.clear();
        playerAccessAttempts.clear();
        playerAccessDenied.clear();
        dimensionUptime.clear();
        dimensionLastOpened.clear();
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        saveMetrics();
    }
}