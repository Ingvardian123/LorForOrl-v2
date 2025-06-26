package com.lorfororl.commands;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.achievements.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AchievementCommand implements CommandExecutor, TabCompleter {
    
    private final LorForOrlPlugin plugin;
    
    public AchievementCommand(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showPlayerAchievements(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                showAllAchievements(player);
                break;
            case "progress":
                showProgress(player);
                break;
            default:
                showPlayerAchievements(player);
                break;
        }
        
        return true;
    }
    
    private void showPlayerAchievements(Player player) {
        List<Achievement> playerAchievements = plugin.getAchievementManager().getPlayerAchievements(player);
        
        player.sendMessage(ChatColor.GREEN + "=== Ваши достижения ===");
        
        if (playerAchievements.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "У вас пока нет достижений.");
            player.sendMessage(ChatColor.YELLOW + "Используйте /achievements list для просмотра всех достижений");
            return;
        }
        
        for (Achievement achievement : playerAchievements) {
            player.sendMessage(String.format("§a✓ %s %s", 
                achievement.getType().getDisplayName(), achievement.getName()));
            player.sendMessage("  §7" + achievement.getDescription());
        }
        
        player.sendMessage(String.format("§eПрогресс: %d/%d достижений", 
            playerAchievements.size(), plugin.getAchievementManager().getAllAchievements().size()));
    }
    
    private void showAllAchievements(Player player) {
        List<Achievement> allAchievements = plugin.getAchievementManager().getAllAchievements();
        
        player.sendMessage(ChatColor.GREEN + "=== Все достижения ===");
        
        for (Achievement achievement : allAchievements) {
            boolean hasAchievement = plugin.getAchievementManager().hasAchievement(player, achievement.getId());
            String status = hasAchievement ? "§a✓" : "§7✗";
            
            player.sendMessage(String.format("%s %s %s", 
                status, achievement.getType().getDisplayName(), achievement.getName()));
            player.sendMessage("  §7" + achievement.getDescription());
            
            if (!hasAchievement && achievement.getRequiredProgress() > 1) {
                int progress = plugin.getAchievementManager().getProgress(player, achievement.getId());
                player.sendMessage(String.format("  §eПрогресс: %d/%d", progress, achievement.getRequiredProgress()));
            }
        }
    }
    
    private void showProgress(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== Прогресс достижений ===");
        
        List<Achievement> allAchievements = plugin.getAchievementManager().getAllAchievements();
        
        for (Achievement achievement : allAchievements) {
            if (achievement.getRequiredProgress() <= 1) continue;
            
            boolean hasAchievement = plugin.getAchievementManager().hasAchievement(player, achievement.getId());
            if (hasAchievement) continue;
            
            int progress = plugin.getAchievementManager().getProgress(player, achievement.getId());
            if (progress == 0) continue;
            
            double percentage = (double) progress / achievement.getRequiredProgress() * 100;
            
            player.sendMessage(String.format("§e%s: §a%d§7/§a%d §7(%.1f%%)", 
                achievement.getName(), progress, achievement.getRequiredProgress(), percentage));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "progress"));
        }
        
        return completions;
    }
}
