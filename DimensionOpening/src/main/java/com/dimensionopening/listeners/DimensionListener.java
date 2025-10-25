package com.dimensionopening.listeners;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import com.dimensionopening.DimensionOpeningPlugin;
import com.dimensionopening.managers.DimensionManager;
import com.dimensionopening.notifications.NotificationManager;
import com.dimensionopening.permissions.PermissionManager;

public class DimensionListener implements Listener {
    
    private final DimensionOpeningPlugin plugin;
    private final DimensionManager dimensionManager;
    private final NotificationManager notificationManager;
    private final PermissionManager permissionManager;
    
    public DimensionListener(DimensionOpeningPlugin plugin, DimensionManager dimensionManager, NotificationManager notificationManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.notificationManager = notificationManager;
        this.permissionManager = permissionManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Check if destination world exists and get its environment
        if (event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }
        
        World destinationWorld = event.getTo().getWorld();
        World.Environment destinationEnvironment = destinationWorld.getEnvironment();
        
        // Check if player has bypass permission or dimension-specific access
        if (permissionManager.canBypassRestrictions(player) || 
            permissionManager.canAccessDimension(player, destinationEnvironment)) {
            return;
        }
        
        // Check if destination dimension is closed
        if (!dimensionManager.isDimensionOpen(destinationEnvironment)) {
            // Allow operators to bypass
            if (player.isOp()) {
                player.sendMessage(ChatColor.YELLOW + "You bypassed the closed " + 
                                 getDimensionDisplayName(destinationEnvironment) + " dimension as an operator.");
                return;
            }
            
            // Cancel teleportation for non-operators
            event.setCancelled(true);
            
            // Record metrics
            plugin.getMetricsManager().recordPlayerAccessAttempt(player, destinationEnvironment.name().toLowerCase());
            plugin.getMetricsManager().recordPlayerAccessDenied(player, destinationEnvironment.name().toLowerCase());
            
            notificationManager.notifyDimensionClosed(player, destinationEnvironment);
            notificationManager.notifyAdminsAccessAttempt(player, destinationEnvironment);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        // Check if destination world exists and get its environment
        if (event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }
        
        World destinationWorld = event.getTo().getWorld();
        World.Environment destinationEnvironment = destinationWorld.getEnvironment();
        
        // Check if player has bypass permission or dimension-specific access
        if (permissionManager.canBypassRestrictions(player) || 
            permissionManager.canAccessDimension(player, destinationEnvironment)) {
            return;
        }
        
        // Check if destination dimension is closed
        if (!dimensionManager.isDimensionOpen(destinationEnvironment)) {
            // Allow operators to bypass
            if (player.isOp()) {
                player.sendMessage(ChatColor.YELLOW + "You bypassed the closed " + 
                                 getDimensionDisplayName(destinationEnvironment) + " dimension as an operator.");
                return;
            }
            
            // Cancel portal usage for non-operators
            event.setCancelled(true);
            
            // Record metrics
            plugin.getMetricsManager().recordPlayerAccessAttempt(player, destinationEnvironment.name().toLowerCase());
            plugin.getMetricsManager().recordPlayerAccessDenied(player, destinationEnvironment.name().toLowerCase());
            
            notificationManager.notifyDimensionClosed(player, destinationEnvironment);
            notificationManager.notifyAdminsAccessAttempt(player, destinationEnvironment);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        notificationManager.sendWelcomeMessage(player);
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