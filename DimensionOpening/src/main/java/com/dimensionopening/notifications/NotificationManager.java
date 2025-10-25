package com.dimensionopening.notifications;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import com.dimensionopening.DimensionOpeningPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationManager {
    
    private final DimensionOpeningPlugin plugin;
    private final Map<UUID, BossBar> activeBossBars;
    
    public NotificationManager(DimensionOpeningPlugin plugin) {
        this.plugin = plugin;
        this.activeBossBars = new HashMap<>();
    }
    
    /**
     * Send notification when a player tries to access a closed dimension
     */
    public void notifyDimensionClosed(Player player, World.Environment dimension) {
        String dimensionName = getDimensionDisplayName(dimension);
        
        // Chat message
        player.sendMessage(ChatColor.RED + "✗ Access Denied!");
        player.sendMessage(ChatColor.GRAY + "The " + ChatColor.YELLOW + dimensionName + 
                          ChatColor.GRAY + " dimension is currently " + ChatColor.RED + "closed" + 
                          ChatColor.GRAY + ".");
        player.sendMessage(ChatColor.GRAY + "Contact an administrator for access.");
        
        // Sound effect
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        
        // Boss bar notification
        showBossBarNotification(player, 
            ChatColor.RED + "Dimension Closed: " + dimensionName, 
            BarColor.RED, 
            3); // 3 seconds
        
        // Title/Subtitle
        player.sendTitle(
            ChatColor.RED + "Access Denied",
            ChatColor.GRAY + dimensionName + " dimension is closed",
            10, 40, 10
        );
    }
    
    /**
     * Notify all players when a dimension state changes
     */
    public void broadcastDimensionStateChange(World.Environment dimension, boolean isOpen) {
        String dimensionName = getDimensionDisplayName(dimension);
        String status = isOpen ? "opened" : "closed";
        ChatColor statusColor = isOpen ? ChatColor.GREEN : ChatColor.RED;
        String symbol = isOpen ? "✓" : "✗";
        
        String message = ChatColor.YELLOW + "[DimensionControl] " + 
                        statusColor + symbol + " " + dimensionName + 
                        ChatColor.YELLOW + " dimension has been " + 
                        statusColor + status + ChatColor.YELLOW + "!";
        
        Bukkit.broadcastMessage(message);
        
        // Play sound to all players
        Sound sound = isOpen ? Sound.BLOCK_NOTE_BLOCK_CHIME : Sound.BLOCK_NOTE_BLOCK_BASS;
        float pitch = isOpen ? 1.5f : 0.5f;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 0.5f, pitch);
            
            // Show action bar message
            player.sendActionBar(statusColor + symbol + " " + dimensionName + " " + status);
        }
    }
    
    /**
     * Notify administrators about dimension access attempts
     */
    public void notifyAdminsAccessAttempt(Player player, World.Environment dimension) {
        String dimensionName = getDimensionDisplayName(dimension);
        String message = ChatColor.GRAY + "[Admin] " + ChatColor.YELLOW + player.getName() + 
                        ChatColor.GRAY + " tried to access the closed " + 
                        ChatColor.RED + dimensionName + ChatColor.GRAY + " dimension.";
        
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("dimensionopening.admin") || admin.isOp()) {
                admin.sendMessage(message);
            }
        }
    }
    
    /**
     * Show a temporary boss bar notification
     */
    public void showBossBarNotification(Player player, String message, BarColor color, int durationSeconds) {
        // Remove existing boss bar if any
        removeBossBar(player);
        
        BossBar bossBar = Bukkit.createBossBar(message, color, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        
        activeBossBars.put(player.getUniqueId(), bossBar);
        
        // Schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeBossBar(player);
        }, durationSeconds * 20L); // Convert seconds to ticks
    }
    
    /**
     * Remove boss bar for a player
     */
    public void removeBossBar(Player player) {
        BossBar existingBar = activeBossBars.remove(player.getUniqueId());
        if (existingBar != null) {
            existingBar.removeAll();
        }
    }
    
    /**
     * Send welcome message to players when they join
     */
    public void sendWelcomeMessage(Player player) {
        if (!plugin.getConfig().getBoolean("notifications.welcome_message", true)) {
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "=== Dimension Status ===");
            
            // Show current dimension states
            for (World.Environment env : World.Environment.values()) {
                if (env == World.Environment.CUSTOM) continue;
                
                String name = getDimensionDisplayName(env);
                boolean isOpen = plugin.getDimensionManager().isDimensionOpen(env);
                ChatColor color = isOpen ? ChatColor.GREEN : ChatColor.RED;
                String symbol = isOpen ? "✓" : "✗";
                
                player.sendMessage(ChatColor.GRAY + "• " + color + symbol + " " + name + 
                                 ChatColor.GRAY + " - " + color + (isOpen ? "Open" : "Closed"));
            }
            
            player.sendMessage("");
        }, 40L); // 2 seconds delay
    }
    
    /**
     * Notify about scheduled dimension changes
     */
    public void notifyScheduledChange(World.Environment dimension, boolean willOpen, int minutesUntil) {
        String dimensionName = getDimensionDisplayName(dimension);
        String action = willOpen ? "open" : "close";
        ChatColor actionColor = willOpen ? ChatColor.GREEN : ChatColor.RED;
        
        String message = ChatColor.YELLOW + "[Scheduled] " + 
                        ChatColor.GRAY + "The " + dimensionName + 
                        ChatColor.GRAY + " dimension will " + actionColor + action + 
                        ChatColor.GRAY + " in " + ChatColor.AQUA + minutesUntil + 
                        ChatColor.GRAY + " minute" + (minutesUntil != 1 ? "s" : "") + ".";
        
        Bukkit.broadcastMessage(message);
        
        // Play notification sound
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
        }
    }
    
    /**
     * Clean up all boss bars when plugin disables
     */
    public void cleanup() {
        for (BossBar bossBar : activeBossBars.values()) {
            bossBar.removeAll();
        }
        activeBossBars.clear();
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