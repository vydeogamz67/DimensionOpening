package com.dimensionopening.managers;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import com.dimensionopening.DimensionOpeningPlugin;
import com.dimensionopening.metrics.MetricsManager;
import java.util.HashMap;
import java.util.Map;

public class DimensionManager {
    
    private final DimensionOpeningPlugin plugin;
    private final Map<World.Environment, Boolean> dimensionStates;
    
    public DimensionManager(DimensionOpeningPlugin plugin) {
        this.plugin = plugin;
        this.dimensionStates = new HashMap<>();
        
        // Initialize default states (all dimensions open by default)
        loadDimensionStates();
    }
    
    private void loadDimensionStates() {
        FileConfiguration config = plugin.getConfig();
        
        // Load states from config, default to true (open) if not set
        dimensionStates.put(World.Environment.NORMAL, config.getBoolean("dimensions.overworld.open", true));
        dimensionStates.put(World.Environment.NETHER, config.getBoolean("dimensions.nether.open", true));
        dimensionStates.put(World.Environment.THE_END, config.getBoolean("dimensions.end.open", true));
    }
    
    private void saveDimensionStates() {
        FileConfiguration config = plugin.getConfig();
        
        config.set("dimensions.overworld.open", dimensionStates.get(World.Environment.NORMAL));
        config.set("dimensions.nether.open", dimensionStates.get(World.Environment.NETHER));
        config.set("dimensions.end.open", dimensionStates.get(World.Environment.THE_END));
        
        plugin.saveConfig();
    }
    
    public boolean isDimensionOpen(World.Environment environment) {
        return dimensionStates.getOrDefault(environment, true);
    }
    
    public boolean openDimension(World.Environment environment) {
        if (isDimensionOpen(environment)) {
            return false; // Already open
        }
        
        dimensionStates.put(environment, true);
        saveDimensionStates();
        
        // Record metrics
        MetricsManager metricsManager = plugin.getMetricsManager();
        if (metricsManager != null) {
            metricsManager.recordDimensionOpen(environment.name().toLowerCase());
        }
        
        return true;
    }
    
    public boolean closeDimension(World.Environment environment) {
        if (!isDimensionOpen(environment)) {
            return false; // Already closed
        }
        
        dimensionStates.put(environment, false);
        saveDimensionStates();
        
        // Record metrics
        MetricsManager metricsManager = plugin.getMetricsManager();
        if (metricsManager != null) {
            metricsManager.recordDimensionClose(environment.name().toLowerCase());
        }
        
        return true;
    }
    
    public String getDimensionStatus(World.Environment environment) {
        return isDimensionOpen(environment) ? "Open" : "Closed";
    }
    
    public Map<World.Environment, Boolean> getAllDimensionStates() {
        return new HashMap<>(dimensionStates);
    }
}