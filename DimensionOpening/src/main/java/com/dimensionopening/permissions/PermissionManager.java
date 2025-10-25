package com.dimensionopening.permissions;

import org.bukkit.World;
import org.bukkit.entity.Player;
import com.dimensionopening.DimensionOpeningPlugin;

public class PermissionManager {
    
    private final DimensionOpeningPlugin plugin;
    
    // Permission nodes
    public static final String ADMIN_PERMISSION = "dimensionopening.admin";
    public static final String BYPASS_PERMISSION = "dimensionopening.bypass";
    
    // Dimension-specific permissions
    public static final String ACCESS_OVERWORLD = "dimensionopening.access.overworld";
    public static final String ACCESS_NETHER = "dimensionopening.access.nether";
    public static final String ACCESS_END = "dimensionopening.access.end";
    
    // Command permissions
    public static final String COMMAND_OPEN = "dimensionopening.command.open";
    public static final String COMMAND_CLOSE = "dimensionopening.command.close";
    public static final String COMMAND_STATUS = "dimensionopening.command.status";
    public static final String COMMAND_GUI = "dimensionopening.command.gui";
    public static final String COMMAND_SCHEDULE = "dimensionopening.command.schedule";
    
    public PermissionManager(DimensionOpeningPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if a player has admin permissions
     */
    public boolean hasAdminPermission(Player player) {
        return player.hasPermission(ADMIN_PERMISSION) || player.isOp();
    }
    
    /**
     * Check if a player can bypass dimension restrictions
     */
    public boolean canBypassRestrictions(Player player) {
        return player.hasPermission(BYPASS_PERMISSION) || 
               hasAdminPermission(player) || 
               (plugin.getConfig().getBoolean("settings.ops_bypass_restrictions", true) && player.isOp());
    }
    
    /**
     * Check if a player can access a specific dimension
     */
    public boolean canAccessDimension(Player player, World.Environment environment) {
        // Bypass check first
        if (canBypassRestrictions(player)) {
            return true;
        }
        
        // Check dimension-specific permission
        String permission = getDimensionAccessPermission(environment);
        if (permission != null && player.hasPermission(permission)) {
            return true;
        }
        
        // Check if dimension is open (default behavior)
        return plugin.getDimensionManager().isDimensionOpen(environment);
    }
    
    /**
     * Check if a player can use a specific command
     */
    public boolean canUseCommand(Player player, String commandType) {
        // Admin permission grants all command access
        if (hasAdminPermission(player)) {
            return true;
        }
        
        // Check specific command permission
        String permission = getCommandPermission(commandType);
        return permission != null && player.hasPermission(permission);
    }
    
    /**
     * Get the permission node for accessing a specific dimension
     */
    public String getDimensionAccessPermission(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return ACCESS_OVERWORLD;
            case NETHER:
                return ACCESS_NETHER;
            case THE_END:
                return ACCESS_END;
            default:
                return null;
        }
    }
    
    /**
     * Get the permission node for a specific command
     */
    public String getCommandPermission(String commandType) {
        switch (commandType.toLowerCase()) {
            case "open":
                return COMMAND_OPEN;
            case "close":
                return COMMAND_CLOSE;
            case "status":
                return COMMAND_STATUS;
            case "gui":
                return COMMAND_GUI;
            case "schedule":
                return COMMAND_SCHEDULE;
            default:
                return null;
        }
    }
    
    /**
     * Get a user-friendly dimension name
     */
    public String getDimensionDisplayName(World.Environment environment) {
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
    
    /**
     * Check if fine-grained permissions are enabled
     */
    public boolean isFineGrainedPermissionsEnabled() {
        return plugin.getConfig().getBoolean("permissions.fine_grained", false);
    }
    
    /**
     * Get permission explanation for a player
     */
    public String getPermissionExplanation(Player player, World.Environment environment) {
        if (canBypassRestrictions(player)) {
            return "You have bypass permissions for all dimensions.";
        }
        
        String dimensionName = getDimensionDisplayName(environment);
        String permission = getDimensionAccessPermission(environment);
        
        if (player.hasPermission(permission)) {
            return "You have specific access to the " + dimensionName + " dimension.";
        }
        
        boolean dimensionOpen = plugin.getDimensionManager().isDimensionOpen(environment);
        if (dimensionOpen) {
            return "The " + dimensionName + " dimension is currently open to all players.";
        } else {
            return "The " + dimensionName + " dimension is closed. You need the '" + permission + "' permission or admin access.";
        }
    }
}