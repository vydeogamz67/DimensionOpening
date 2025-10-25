package com.dimensionopening.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.dimensionopening.DimensionOpeningPlugin;
import com.dimensionopening.managers.DimensionManager;
import com.dimensionopening.notifications.NotificationManager;
import com.dimensionopening.permissions.PermissionManager;
import java.util.Map;

public class DimensionCommand implements CommandExecutor {
    
    private final DimensionOpeningPlugin plugin;
    private final DimensionManager dimensionManager;
    private final NotificationManager notificationManager;
    private final PermissionManager permissionManager;
    
    public DimensionCommand(DimensionOpeningPlugin plugin, DimensionManager dimensionManager, NotificationManager notificationManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.notificationManager = notificationManager;
        this.permissionManager = permissionManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has admin permission (includes operators)
        if (sender instanceof Player && !permissionManager.hasAdminPermission((Player) sender)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        } else if (!(sender instanceof Player) && !sender.hasPermission("dimensionopening.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Check if no arguments provided
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        // Handle status command
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            if (sender instanceof Player && !permissionManager.canUseCommand((Player) sender, "status")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to check dimension status!");
                return true;
            }
            sendStatusMessage(sender);
            return true;
        }
        
        // Handle GUI command
        if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
            if (sender instanceof Player && !permissionManager.canUseCommand((Player) sender, "gui")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use the dimension GUI!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use the GUI!");
                return true;
            }
            plugin.getDimensionGUI().openDimensionGUI((Player) sender);
            return true;
        }
        
        // Check arguments for open/close commands
        if (args.length < 2) {
            sendHelpMessage(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        String dimensionName = args.length > 1 ? args[1].toLowerCase() : "";
        
        // Validate action
        if (!action.equals("open") && !action.equals("close")) {
            sender.sendMessage(ChatColor.RED + "Invalid action! Use 'open' or 'close'");
            return true;
        }
        
        // Validate dimension
        World.Environment environment;
        String displayName;
        switch (dimensionName) {
            case "world":
            case "overworld":
                environment = World.Environment.NORMAL;
                displayName = "Overworld";
                break;
            case "nether":
                environment = World.Environment.NETHER;
                displayName = "Nether";
                break;
            case "end":
                environment = World.Environment.THE_END;
                displayName = "End";
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid dimension! Use 'world', 'nether', or 'end'");
                return true;
        }
        
        // Execute action
        boolean success;
        if (action.equals("open")) {
            if (sender instanceof Player && !permissionManager.canUseCommand((Player) sender, "open")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to open dimensions!");
                return true;
            }
            
            success = dimensionManager.openDimension(environment);
            if (success) {
                sender.sendMessage(ChatColor.GREEN + displayName + " dimension has been opened!");
                notificationManager.broadcastDimensionStateChange(environment, true);
            } else {
                sender.sendMessage(ChatColor.YELLOW + displayName + " dimension is already open!");
            }
        } else {
            if (sender instanceof Player && !permissionManager.canUseCommand((Player) sender, "close")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to close dimensions!");
                return true;
            }
            
            success = dimensionManager.closeDimension(environment);
            if (success) {
                sender.sendMessage(ChatColor.GREEN + displayName + " dimension has been closed!");
                notificationManager.broadcastDimensionStateChange(environment, false);
            } else {
                sender.sendMessage(ChatColor.YELLOW + displayName + " dimension is already closed!");
            }
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== DimensionOpening Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/dimension status" + ChatColor.WHITE + " - Show dimension states");
        sender.sendMessage(ChatColor.YELLOW + "/dimension gui" + ChatColor.WHITE + " - Open dimension GUI (players only)");
        sender.sendMessage(ChatColor.YELLOW + "/dimension open <dimension>" + ChatColor.WHITE + " - Open a dimension");
        sender.sendMessage(ChatColor.YELLOW + "/dimension close <dimension>" + ChatColor.WHITE + " - Close a dimension");
        sender.sendMessage(ChatColor.GRAY + "Dimensions: world, nether, end");
    }
    
    private void sendStatusMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Dimension Status ===");
        
        // Get all dimension states
        Map<World.Environment, Boolean> states = dimensionManager.getAllDimensionStates();
        
        // Display each dimension status
        for (Map.Entry<World.Environment, Boolean> entry : states.entrySet()) {
            String dimensionName = getDimensionDisplayName(entry.getKey());
            boolean isOpen = entry.getValue();
            
            ChatColor statusColor = isOpen ? ChatColor.GREEN : ChatColor.RED;
            String statusText = isOpen ? "OPEN" : "CLOSED";
            
            sender.sendMessage(ChatColor.YELLOW + dimensionName + ": " + statusColor + statusText);
        }
        
        sender.sendMessage(ChatColor.GRAY + "Use /dimension <open|close> <dimension> to change states");
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