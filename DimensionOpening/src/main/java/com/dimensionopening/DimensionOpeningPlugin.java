package com.dimensionopening;

import org.bukkit.plugin.java.JavaPlugin;
import com.dimensionopening.commands.DimensionCommand;
import com.dimensionopening.commands.DimensionTabCompleter;
import com.dimensionopening.listeners.DimensionListener;
import com.dimensionopening.managers.DimensionManager;
import com.dimensionopening.managers.ScheduleManager;
import com.dimensionopening.gui.DimensionGUI;
import com.dimensionopening.notifications.NotificationManager;
import com.dimensionopening.permissions.PermissionManager;
import com.dimensionopening.metrics.MetricsManager;

public class DimensionOpeningPlugin extends JavaPlugin {
    
    private DimensionManager dimensionManager;
    private ScheduleManager scheduleManager;
    private DimensionGUI dimensionGUI;
    private NotificationManager notificationManager;
    private PermissionManager permissionManager;
    private MetricsManager metricsManager;
    
    @Override
    public void onEnable() {
        // Initialize the dimension manager
        this.dimensionManager = new DimensionManager(this);
        
        // Initialize the schedule manager
        this.scheduleManager = new ScheduleManager(this, dimensionManager);
        
        // Initialize notification manager
        notificationManager = new NotificationManager(this);
        
        // Initialize permission manager
        permissionManager = new PermissionManager(this);
        
        // Initialize metrics manager
        metricsManager = new MetricsManager(this);
        
        // Initialize GUI
        this.dimensionGUI = new DimensionGUI(this, dimensionManager, notificationManager, permissionManager);
        getServer().getPluginManager().registerEvents(dimensionGUI, this);
        
        // Register commands
        getCommand("dimension").setExecutor(new DimensionCommand(this, dimensionManager, notificationManager, permissionManager));
        getCommand("dimension").setTabCompleter(new DimensionTabCompleter());
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new DimensionListener(this, dimensionManager, notificationManager, permissionManager), this);
        
        // Save default config
        saveDefaultConfig();
        
        getLogger().info("DimensionOpening plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Cancel all scheduled tasks
        if (scheduleManager != null) {
            scheduleManager.cancelAllSchedules();
        }
        
        if (notificationManager != null) {
            notificationManager.cleanup();
        }
        
        // Cleanup metrics manager
        if (metricsManager != null) {
            metricsManager.cleanup();
        }
        
        getLogger().info("DimensionOpening plugin has been disabled!");
    }
    
    public DimensionManager getDimensionManager() {
        return dimensionManager;
    }
    
    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }
    
    public DimensionGUI getDimensionGUI() {
        return dimensionGUI;
    }
    
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }
}