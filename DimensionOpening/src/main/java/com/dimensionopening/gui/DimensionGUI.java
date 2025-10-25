package com.dimensionopening.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.dimensionopening.DimensionOpeningPlugin;
import com.dimensionopening.managers.DimensionManager;
import com.dimensionopening.notifications.NotificationManager;
import com.dimensionopening.permissions.PermissionManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DimensionGUI implements Listener {
    
    private final DimensionOpeningPlugin plugin;
    private final DimensionManager dimensionManager;
    private final NotificationManager notificationManager;
    private final PermissionManager permissionManager;
    private final Map<Player, Inventory> openGUIs;
    
    public DimensionGUI(DimensionOpeningPlugin plugin, DimensionManager dimensionManager, NotificationManager notificationManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
        this.notificationManager = notificationManager;
        this.permissionManager = permissionManager;
        this.openGUIs = new HashMap<>();
    }
    
    public void openDimensionGUI(Player player) {
        // Check permissions
        if (!permissionManager.canUseCommand(player, "gui")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Dimension Control");
        
        // Overworld (slot 10)
        ItemStack overworldItem = createDimensionItem(
            World.Environment.NORMAL,
            Material.GRASS_BLOCK,
            "Overworld",
            dimensionManager.isDimensionOpen(World.Environment.NORMAL)
        );
        gui.setItem(10, overworldItem);
        
        // Nether (slot 13)
        ItemStack netherItem = createDimensionItem(
            World.Environment.NETHER,
            Material.NETHERRACK,
            "Nether",
            dimensionManager.isDimensionOpen(World.Environment.NETHER)
        );
        gui.setItem(13, netherItem);
        
        // End (slot 16)
        ItemStack endItem = createDimensionItem(
            World.Environment.THE_END,
            Material.END_STONE,
            "End",
            dimensionManager.isDimensionOpen(World.Environment.THE_END)
        );
        gui.setItem(16, endItem);
        
        // Info item (slot 22)
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Information");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click on dimensions to toggle them",
            ChatColor.GRAY + "Green = Open, Red = Closed",
            ChatColor.GRAY + "Only operators can access closed dimensions"
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(22, infoItem);
        
        // Fill empty slots with glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
        
        openGUIs.put(player, gui);
        player.openInventory(gui);
    }
    
    private ItemStack createDimensionItem(World.Environment environment, Material material, String name, boolean isOpen) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        ChatColor statusColor = isOpen ? ChatColor.GREEN : ChatColor.RED;
        String status = isOpen ? "OPEN" : "CLOSED";
        
        meta.setDisplayName(statusColor + name + " Dimension");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Status: " + statusColor + status,
            "",
            ChatColor.YELLOW + "Click to " + (isOpen ? "close" : "open"),
            ChatColor.GRAY + "Players " + (isOpen ? "can" : "cannot") + " access this dimension"
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        // Check if this is our GUI
        if (!openGUIs.containsKey(player) || !openGUIs.get(player).equals(clickedInventory)) {
            return;
        }
        
        event.setCancelled(true); // Prevent item pickup
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        World.Environment environment = null;
        String dimensionName = null;
        
        // Determine which dimension was clicked
        switch (slot) {
            case 10: // Overworld
                environment = World.Environment.NORMAL;
                dimensionName = "Overworld";
                break;
            case 13: // Nether
                environment = World.Environment.NETHER;
                dimensionName = "Nether";
                break;
            case 16: // End
                environment = World.Environment.THE_END;
                dimensionName = "End";
                break;
            default:
                return; // Not a dimension slot
        }
        
        // Check if player has admin permission
        if (!permissionManager.hasAdminPermission(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to modify dimensions!");
            return;
        }
        
        // Toggle dimension state
        boolean currentState = dimensionManager.isDimensionOpen(environment);
        boolean success;
        
        if (currentState) {
            success = dimensionManager.closeDimension(environment);
            if (success) {
                player.sendMessage(ChatColor.RED + dimensionName + " dimension has been closed!");
                notificationManager.broadcastDimensionStateChange(environment, false);
            }
        } else {
            success = dimensionManager.openDimension(environment);
            if (success) {
                player.sendMessage(ChatColor.GREEN + dimensionName + " dimension has been opened!");
                notificationManager.broadcastDimensionStateChange(environment, true);
            }
        }
        
        // Refresh the GUI
        if (success) {
            refreshGUI(player);
        }
    }
    
    private void refreshGUI(Player player) {
        Inventory gui = openGUIs.get(player);
        if (gui == null) return;
        
        // Update dimension items
        gui.setItem(10, createDimensionItem(
            World.Environment.NORMAL,
            Material.GRASS_BLOCK,
            "Overworld",
            dimensionManager.isDimensionOpen(World.Environment.NORMAL)
        ));
        
        gui.setItem(13, createDimensionItem(
            World.Environment.NETHER,
            Material.NETHERRACK,
            "Nether",
            dimensionManager.isDimensionOpen(World.Environment.NETHER)
        ));
        
        gui.setItem(16, createDimensionItem(
            World.Environment.THE_END,
            Material.END_STONE,
            "End",
            dimensionManager.isDimensionOpen(World.Environment.THE_END)
        ));
    }
    
    public void closeGUI(Player player) {
        openGUIs.remove(player);
    }
}