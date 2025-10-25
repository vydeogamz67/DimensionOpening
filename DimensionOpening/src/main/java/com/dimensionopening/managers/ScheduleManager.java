package com.dimensionopening.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.dimensionopening.DimensionOpeningPlugin;
import java.util.HashMap;
import java.util.Map;

public class ScheduleManager {
    
    private final DimensionOpeningPlugin plugin;
    private final DimensionManager dimensionManager;
    private final Map<String, BukkitTask> scheduledTasks;
    
    public ScheduleManager(DimensionOpeningPlugin plugin, DimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.scheduledTasks = new HashMap<>();
        
        loadSchedules();
    }
    
    private void loadSchedules() {
        ConfigurationSection scheduleSection = plugin.getConfig().getConfigurationSection("schedules");
        if (scheduleSection == null) {
            return;
        }
        
        for (String scheduleName : scheduleSection.getKeys(false)) {
            ConfigurationSection schedule = scheduleSection.getConfigurationSection(scheduleName);
            if (schedule == null) continue;
            
            boolean enabled = schedule.getBoolean("enabled", false);
            if (!enabled) continue;
            
            String dimensionName = schedule.getString("dimension");
            String action = schedule.getString("action");
            long intervalTicks = schedule.getLong("interval_ticks", 24000); // Default: 1 day
            long delayTicks = schedule.getLong("delay_ticks", 0);
            
            World.Environment environment = parseDimension(dimensionName);
            if (environment == null) {
                plugin.getLogger().warning("Invalid dimension in schedule: " + dimensionName);
                continue;
            }
            
            if (!action.equals("open") && !action.equals("close")) {
                plugin.getLogger().warning("Invalid action in schedule: " + action);
                continue;
            }
            
            scheduleTask(scheduleName, environment, action, delayTicks, intervalTicks);
        }
    }
    
    private void scheduleTask(String name, World.Environment environment, String action, long delay, long interval) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                boolean success;
                String dimensionName = getDimensionDisplayName(environment);
                
                if (action.equals("open")) {
                    success = dimensionManager.openDimension(environment);
                    if (success) {
                        Bukkit.broadcastMessage(ChatColor.GREEN + "[Scheduled] " + dimensionName + " dimension has been opened!");
                    }
                } else {
                    success = dimensionManager.closeDimension(environment);
                    if (success) {
                        Bukkit.broadcastMessage(ChatColor.RED + "[Scheduled] " + dimensionName + " dimension has been closed!");
                    }
                }
                
                plugin.getLogger().info("Scheduled " + action + " for " + dimensionName + " executed: " + success);
            }
        }.runTaskTimer(plugin, delay, interval);
        
        scheduledTasks.put(name, task);
        plugin.getLogger().info("Scheduled task '" + name + "' registered: " + action + " " + getDimensionDisplayName(environment) + " every " + interval + " ticks");
    }
    
    public void cancelSchedule(String name) {
        BukkitTask task = scheduledTasks.remove(name);
        if (task != null) {
            task.cancel();
        }
    }
    
    public void cancelAllSchedules() {
        for (BukkitTask task : scheduledTasks.values()) {
            task.cancel();
        }
        scheduledTasks.clear();
    }
    
    public Map<String, BukkitTask> getActiveSchedules() {
        return new HashMap<>(scheduledTasks);
    }
    
    private World.Environment parseDimension(String dimensionName) {
        if (dimensionName == null) return null;
        
        switch (dimensionName.toLowerCase()) {
            case "world":
            case "overworld":
                return World.Environment.NORMAL;
            case "nether":
                return World.Environment.NETHER;
            case "end":
                return World.Environment.THE_END;
            default:
                return null;
        }
    }
    
    private String getDimensionDisplayName(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return "Overworld";
            case NETHER:
                return "Nether";
            case THE_END:
                return "End";
            default:
                return "Unknown";
        }
    }
}