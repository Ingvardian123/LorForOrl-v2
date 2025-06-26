package com.lorfororl.commands;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LorForOrlCommand implements CommandExecutor {
    
    private final LorForOrlPlugin plugin;
    
    public LorForOrlCommand(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(player);
                break;
            case "info":
                showInfo(player);
                break;
            case "radiation":
                showRadiation(player);
                break;
            case "debug":
                toggleDebug(player);
                break;
            case "gui":
                plugin.getGuiManager().openMainGui(player);
                break;
            default:
                player.sendMessage("§cНеизвестная команда! Используйте /lorfororl help");
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== LorForOrl v2.0 ===");
        player.sendMessage("§e/lorfororl help §7- Показать справку");
        player.sendMessage("§e/lorfororl info §7- Информация о плагине");
        player.sendMessage("§e/lorfororl radiation §7- Показать уровень радиации");
        player.sendMessage("§e/lorfororl debug §7- Переключить режим отладки");
        player.sendMessage("§e/lorfororl gui §7- Открыть главное меню");
    }
    
    private void showInfo(Player player) {
        long uptime = System.currentTimeMillis() - plugin.getStartTime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        player.sendMessage("§6=== LorForOrl v2.0 ===");
        player.sendMessage("§eВремя работы: §7" + hours + "ч " + (minutes % 60) + "м " + (seconds % 60) + "с");
        player.sendMessage("§eАвтор: §7LorForOrl Team");
        player.sendMessage("§eВерсия: §72.0.0");
    }
    
    private void showRadiation(Player player) {
        double radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        player.sendMessage("§6Уровень радиации: §c" + String.format("%.4f", radiation) + " §6у.е.");
        
        if (radiation < 0.5) {
            player.sendMessage("§aСостояние: Безопасно");
        } else if (radiation < 1.5) {
            player.sendMessage("§eСостояние: Легкое облучение");
        } else if (radiation < 3.0) {
            player.sendMessage("§cСостояние: Опасное облучение");
        } else {
            player.sendMessage("§4Состояние: Критическое облучение!");
        }
    }
    
    private void toggleDebug(Player player) {
        boolean enabled = plugin.getRadiationManager().toggleDebugMode(player);
        if (enabled) {
            player.sendMessage("§aРежим отладки включен");
        } else {
            player.sendMessage("§cРежим отладки выключен");
        }
    }
}
