package com.dimensionopening.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DimensionTabCompleter implements TabCompleter {
    
    private static final List<String> ACTIONS = Arrays.asList("open", "close", "status", "gui");
    private static final List<String> DIMENSIONS = Arrays.asList("world", "overworld", "nether", "end");
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Check if sender has permission
        if (!sender.hasPermission("dimensionopening.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument: action (open, close, status)
            String input = args[0].toLowerCase();
            for (String action : ACTIONS) {
                if (action.startsWith(input)) {
                    completions.add(action);
                }
            }
        } else if (args.length == 2) {
            // Second argument: dimension (only for open/close, not status)
            String action = args[0].toLowerCase();
            if (action.equals("open") || action.equals("close")) {
                String input = args[1].toLowerCase();
                for (String dimension : DIMENSIONS) {
                    if (dimension.startsWith(input)) {
                        completions.add(dimension);
                    }
                }
            }
        }
        
        return completions;
    }
}