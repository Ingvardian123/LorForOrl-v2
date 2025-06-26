package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HudManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, PlayerHudData> playerHuds;
    private BukkitRunnable hudTask;
    
    public HudManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerHuds = new HashMap<>();
        startHudTask();
    }
    
    private void startHudTask() {
        hudTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllHuds();
            }
        };
        hudTask.runTaskTimer(plugin, 0L, 10L); // Каждые 0.5 секунды
    }
    
    private void updateAllHuds() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerHudData hudData = playerHuds.computeIfAbsent(player.getUniqueId(), k -> new PlayerHudData());
            updatePlayerHud(player, hudData);
        }
    }
    
    private void updatePlayerHud(Player player, PlayerHudData hudData) {
        StringBuilder hud = new StringBuilder();
        
        // Энергия
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        String energyBar = createAdvancedBar(energy, 1000, "⚡", "§e", "§7", 15);
        hud.append("§6⚡ ").append(energyBar).append(" ");
        
        // Радиация
        double radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        if (radiation > 0.1) {
            String radiationColor = radiation > 3.0 ? "§4" : radiation > 1.5 ? "§c" : "§e";
            String radiationIcon = getRadiationIcon(radiation);
            hud.append(radiationColor).append(radiationIcon).append(" ").append(String.format("%.1f", radiation)).append(" ");
        }
        
        // Силовая броня
        if (plugin.getPowerArmorSystem().getArmorData(player) != null) {
            var armorData = plugin.getPowerArmorSystem().getArmorData(player);
            String modeColor = getModeColor(armorData.getMode());
            String modeIcon = getModeIcon(armorData.getMode());
            hud.append("§7| ").append(modeColor).append(modeIcon).append(" ").append(armorData.getMode().getDisplayName()).append(" ");
        }
        
        // Рельсотрон
        if (plugin.getRailgunSystem().getRailgunData(player) != null) {
            var railgunData = plugin.getRailgunSystem().getRailgunData(player);
            if (railgunData.isOnCooldown()) {
                long remaining = railgunData.getRemainingCooldown() / 1000;
                hud.append("§7| §c🔫 ").append(remaining).append("s ");
            } else {
                String modeColor = getRailgunModeColor(railgunData.getMode());
                hud.append("§7| ").append(modeColor).append("🔫 ГОТОВ ");
            }
        }
        
        // Активное исследование
        var activeResearch = plugin.getResearchManager().getActiveResearch(player);
        if (activeResearch != null) {
            double progress = activeResearch.getProgress() * 100;
            String progressBar = createMiniBar((int)progress, 100, "§b", "§7", 8);
            hud.append("§7| §3📚 ").append(progressBar).append(" ");
        }
        
        // Отправляем HUD
        String finalHud = hud.toString().trim();
        if (!finalHud.equals(hudData.getLastHud())) {
            player.sendActionBar(finalHud);
            hudData.setLastHud(finalHud);
        }
        
        // Дополнительные уведомления
        checkForWarnings(player, hudData);
    }
    
    private void checkForWarnings(Player player, PlayerHudData hudData) {
        long currentTime = System.currentTimeMillis();
        
        // Предупреждение о низкой энергии
        int energy = plugin.getEnergyManager().getPlayerEnergy(player);
        if (energy < 100 && currentTime - hudData.getLastEnergyWarning() > 10000) {
            player.sendTitle("", "§c⚡ НИЗКИЙ ЗАРЯД ЭНЕРГИИ! ⚡", 5, 20, 5);
            hudData.setLastEnergyWarning(currentTime);
        }
        
        // Предупреждение о радиации
        double radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        if (radiation > 3.0 && currentTime - hudData.getLastRadiationWarning() > 5000) {
            player.sendTitle("§4☢ ОПАСНАЯ РАДИАЦИЯ! ☢", "§cНайдите укрытие или используйте защиту!", 10, 30, 10);
            hudData.setLastRadiationWarning(currentTime);
        }
    }
    
    private String createAdvancedBar(int current, int max, String icon, String filledColor, String emptyColor, int length) {
        int filled = (int) ((double) current / max * length);
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append(filledColor).append("█");
            } else {
                bar.append(emptyColor).append("█");
            }
        }
        
        // Добавляем числовые значения
        bar.append(" §f").append(current).append("§7/§f").append(max);
        
        return bar.toString();
    }
    
    private String createMiniBar(int current, int max, String filledColor, String emptyColor, int length) {
        int filled = (int) ((double) current / max * length);
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append(filledColor).append("▌");
            } else {
                bar.append(emptyColor).append("▌");
            }
        }
        
        bar.append(" §f").append(current).append("%");
        
        return bar.toString();
    }
    
    private String getRadiationIcon(double radiation) {
        if (radiation > 5.0) return "☢☢☢";
        if (radiation > 3.0) return "☢☢";
        if (radiation > 1.5) return "☢";
        return "⚠";
    }
    
    private String getModeColor(com.lorfororl.equipment.PowerArmorSystem.PowerArmorMode mode) {
        switch (mode) {
            case NORMAL: return "§a";
            case COMBAT: return "§c";
            case STEALTH: return "§5";
            case FLIGHT: return "§b";
            case SHIELD: return "§e";
            case LOW_POWER: return "§8";
            default: return "§7";
        }
    }
    
    private String getModeIcon(com.lorfororl.equipment.PowerArmorSystem.PowerArmorMode mode) {
        switch (mode) {
            case NORMAL: return "🛡";
            case COMBAT: return "⚔";
            case STEALTH: return "👤";
            case FLIGHT: return "🚀";
            case SHIELD: return "🛡";
            case LOW_POWER: return "🔋";
            default: return "⚙";
        }
    }
    
    private String getRailgunModeColor(com.lorfororl.weapons.RailgunSystem.RailgunMode mode) {
        switch (mode) {
            case STANDARD: return "§f";
            case PIERCING: return "§e";
            case EXPLOSIVE: return "§c";
            case SCATTER: return "§6";
            case OVERCHARGE: return "§5";
            case EMP: return "§b";
            default: return "§7";
        }
    }
    
    public void shutdown() {
        if (hudTask != null) {
            hudTask.cancel();
        }
        playerHuds.clear();
    }
    
    private static class PlayerHudData {
        private String lastHud = "";
        private long lastEnergyWarning = 0;
        private long lastRadiationWarning = 0;
        
        public String getLastHud() { return lastHud; }
        public void setLastHud(String lastHud) { this.lastHud = lastHud; }
        public long getLastEnergyWarning() { return lastEnergyWarning; }
        public void setLastEnergyWarning(long lastEnergyWarning) { this.lastEnergyWarning = lastEnergyWarning; }
        public long getLastRadiationWarning() { return lastRadiationWarning; }
        public void setLastRadiationWarning(long lastRadiationWarning) { this.lastRadiationWarning = lastRadiationWarning; }
    }
}
