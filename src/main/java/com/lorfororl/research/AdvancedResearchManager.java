package com.lorfororl.research;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedResearchManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, AdvancedResearch> availableResearch;
    private final Map<UUID, ActiveAdvancedResearch> activeResearch;
    private final Map<UUID, Set<String>> completedResearch;
    private BukkitRunnable researchTask;
    
    public AdvancedResearchManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.availableResearch = new HashMap<>();
        this.activeResearch = new ConcurrentHashMap<>();
        this.completedResearch = new HashMap<>();
        
        initializeResearch();
    }
    
    public void startTasks() {
        researchTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateActiveResearch();
            }
        };
        researchTask.runTaskTimerAsynchronously(plugin, 0L, 1200L); // Каждую минуту
    }
    
    public void shutdown() {
        if (researchTask != null) {
            researchTask.cancel();
        }
    }
    
    private void initializeResearch() {
        // ЯДЕРНЫЙ РЕАКТОР - 240 часов (14400 минут)
        AdvancedResearch nuclearReactor = new AdvancedResearch(
            "nuclear_reactor",
            "§4⚛ Ядерный реактор",
            "Изучение технологии ядерного деления для получения огромного количества энергии",
            Research.ResearchCategory.ENERGY,
            5,
            720 // 12 часов
        );
        nuclearReactor.addRequiredResource(Material.IRON_BLOCK, 500)
                     .addRequiredResource(Material.REDSTONE_BLOCK, 250)
                     .addRequiredResource(Material.COPPER_BLOCK, 100)
                     .addRequiredResource(Material.OBSIDIAN, 200)
                     .addRequiredResource(Material.DIAMOND_BLOCK, 50)
                     .addRequiredResource(Material.GOLD_BLOCK, 75)
                     .addRequiredResource(Material.BEACON, 10);
        // Добавляем урановые блоки через специальный метод
        nuclearReactor.addSpecialResource("uranium_block", 3);
        availableResearch.put("nuclear_reactor", nuclearReactor);
        
        // СИЛОВАЯ БРОНЯ - 120 часов (7200 минут)
        AdvancedResearch powerArmor = new AdvancedResearch(
            "power_armor",
            "§6⚡ Силовая броня",
            "Разработка экзоскелета с энергетическим питанием и защитными системами",
            Research.ResearchCategory.PROTECTION,
            4,
            360 // 6 часов
        );
        powerArmor.addRequiredResource(Material.IRON_BLOCK, 300)
                  .addRequiredResource(Material.DIAMOND_BLOCK, 25)
                  .addRequiredResource(Material.REDSTONE_BLOCK, 150)
                  .addRequiredResource(Material.GOLD_BLOCK, 50)
                  .addRequiredResource(Material.COPPER_BLOCK, 75)
                  .addRequiredResource(Material.NETHERITE_INGOT, 20)
                  .addRequiredResource(Material.BEACON, 5);
        availableResearch.put("power_armor", powerArmor);
        
        // РЕЛЬСОТРОН - 96 часов (5760 минут)
        AdvancedResearch railgun = new AdvancedResearch(
            "railgun",
            "§c⚡ Рельсотрон",
            "Электромагнитное оружие сверхвысокой мощности",
            Research.ResearchCategory.WEAPONS,
            4,
            288 // 4.8 часа
        );
        railgun.addRequiredResource(Material.COPPER_BLOCK, 200)
               .addRequiredResource(Material.IRON_BLOCK, 150)
               .addRequiredResource(Material.REDSTONE_BLOCK, 100)
               .addRequiredResource(Material.GOLD_BLOCK, 30)
               .addRequiredResource(Material.DIAMOND_BLOCK, 15)
               .addRequiredResource(Material.LIGHTNING_ROD, 50)
               .addRequiredResource(Material.BEACON, 3);
        availableResearch.put("railgun", railgun);
        
        // ЗАЩИТНЫЙ КОСТЮМ - 72 часа (4320 минут)
        AdvancedResearch hazmatSuit = new AdvancedResearch(
            "hazmat_suit",
            "§e☣ Защитный костюм",
            "Костюм химической и радиационной защиты",
            Research.ResearchCategory.PROTECTION,
            3,
            180 // 3 часа
        );
        hazmatSuit.addRequiredResource(Material.LEATHER, 200)
                  .addRequiredResource(Material.IRON_INGOT, 100)
                  .addRequiredResource(Material.GLASS, 50)
                  .addRequiredResource(Material.REDSTONE, 150)
                  .addRequiredResource(Material.SLIME_BALL, 64)
                  .addRequiredResource(Material.COAL_BLOCK, 25);
        availableResearch.put("hazmat_suit", hazmatSuit);
        
        // ЭЛЕКТРОТРАНСПОРТ - 48 часов (2880 минут)
        AdvancedResearch electricVehicle = new AdvancedResearch(
            "electric_vehicle",
            "§a🚗 Электротранспорт",
            "Экологически чистый транспорт на электрической тяге",
            Research.ResearchCategory.TRANSPORT,
            3,
            120 // 2 часа
        );
        electricVehicle.addRequiredResource(Material.IRON_BLOCK, 100)
                      .addRequiredResource(Material.COPPER_BLOCK, 50)
                      .addRequiredResource(Material.REDSTONE_BLOCK, 30)
                      .addRequiredResource(Material.MINECART, 10)
                      .addRequiredResource(Material.POWERED_RAIL, 64)
                      .addRequiredResource(Material.REDSTONE, 200); // Заменяем BATTERY на REDSTONE
        availableResearch.put("electric_vehicle", electricVehicle);
        
        // АВТОМАТИЧЕСКИЙ ШАХТЕР - 60 часов (3600 минут)
        AdvancedResearch autoMiner = new AdvancedResearch(
            "auto_miner",
            "§6⛏ Автошахтер",
            "Автоматизированная система добычи ресурсов",
            Research.ResearchCategory.AUTOMATION,
            3,
            150 // 2.5 часа
        );
        autoMiner.addRequiredResource(Material.IRON_BLOCK, 80)
                 .addRequiredResource(Material.DIAMOND_PICKAXE, 5)
                 .addRequiredResource(Material.REDSTONE_BLOCK, 40)
                 .addRequiredResource(Material.HOPPER, 20)
                 .addRequiredResource(Material.DISPENSER, 10)
                 .addRequiredResource(Material.OBSERVER, 15);
        availableResearch.put("auto_miner", autoMiner);
        
        // ЭНЕРГЕТИЧЕСКИЙ ЩИТ - 84 часа (5040 минут)
        AdvancedResearch energyShield = new AdvancedResearch(
            "energy_shield",
            "§b🛡 Энергощит",
            "Силовое поле для защиты территории",
            Research.ResearchCategory.PROTECTION,
            4,
            240 // 4 часа
        );
        energyShield.addRequiredResource(Material.DIAMOND_BLOCK, 40)
                    .addRequiredResource(Material.BEACON, 8)
                    .addRequiredResource(Material.CONDUIT, 5)
                    .addRequiredResource(Material.COPPER_BLOCK, 120)
                    .addRequiredResource(Material.REDSTONE_BLOCK, 80)
                    .addRequiredResource(Material.GOLD_BLOCK, 30);
        availableResearch.put("energy_shield", energyShield);

        // ЯДЕРНАЯ БОМБА - 480 часов (8 часов)
        AdvancedResearch nuclearBomb = new AdvancedResearch(
            "nuclear_bomb",
            "§4💣 Ядерная бомба",
            "Оружие массового поражения с огромной разрушительной силой",
            Research.ResearchCategory.WEAPONS,
            5,
            480 // 8 часов
        );
        nuclearBomb.addRequiredResource(Material.TNT, 500)
                   .addRequiredResource(Material.REDSTONE_BLOCK, 200)
                   .addRequiredResource(Material.IRON_BLOCK, 300)
                   .addRequiredResource(Material.GOLD_BLOCK, 100)
                   .addRequiredResource(Material.DIAMOND_BLOCK, 50)
                   .addRequiredResource(Material.OBSIDIAN, 200)
                   .addRequiredResource(Material.BEACON, 15)
                   .addRequiredResource(Material.NETHER_STAR, 5);
        nuclearBomb.addSpecialResource("uranium_block", 10);
        availableResearch.put("nuclear_bomb", nuclearBomb);
    }
    
    public boolean startResearch(Player player, String researchId) {
        if (activeResearch.containsKey(player.getUniqueId())) {
            player.sendMessage("§c❌ У вас уже идет исследование!");
            return false;
        }
        
        AdvancedResearch research = availableResearch.get(researchId);
        if (research == null) {
            player.sendMessage("§c❌ Исследование не найдено!");
            return false;
        }
        
        if (isResearchCompleted(player, researchId)) {
            player.sendMessage("§c❌ Это исследование уже завершено!");
            return false;
        }
        
        // Проверяем наличие всех ресурсов
        if (!hasRequiredResources(player, research)) {
            player.sendMessage("§c❌ Недостаточно ресурсов для исследования!");
            showRequiredResources(player, research);
            return false;
        }
        
        // Забираем ресурсы
        if (!consumeResources(player, research)) {
            player.sendMessage("§c❌ Ошибка при изъятии ресурсов!");
            return false;
        }
        
        // Запускаем исследование
        ActiveAdvancedResearch active = new ActiveAdvancedResearch(
            researchId, 
            System.currentTimeMillis(),
            research.getResearchTimeMinutes() * 60000L // Переводим в миллисекунды
        );
        
        activeResearch.put(player.getUniqueId(), active);
        
        player.sendMessage("§a✅ Исследование '" + research.getName() + "' начато!");
        player.sendMessage("§e⏰ Время завершения: " + formatTime(research.getResearchTimeMinutes()));
        player.sendMessage("§7💡 Прогресс можно отслеживать в терминале лаборатории");
        
        return true;
    }
    
    private boolean hasRequiredResources(Player player, AdvancedResearch research) {
        // Проверяем обычные ресурсы
        for (Map.Entry<Material, Integer> entry : research.getRequiredResources().entrySet()) {
            if (!hasEnoughItems(player, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        // Проверяем специальные ресурсы (урановые блоки и т.д.)
        for (Map.Entry<String, Integer> entry : research.getSpecialResources().entrySet()) {
            if (!hasEnoughSpecialItems(player, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean hasEnoughItems(Player player, Material material, int required) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= required;
    }
    
    private boolean hasEnoughSpecialItems(Player player, String itemType, int required) {
        if ("uranium_block".equals(itemType)) {
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && plugin.getUraniumItems().isUraniumItem(item)) {
                    String type = plugin.getUraniumItems().getUraniumType(item);
                    if ("block".equals(type)) {
                        count += item.getAmount();
                    }
                }
            }
            return count >= required;
        }
        return false;
    }
    
    private boolean consumeResources(Player player, AdvancedResearch research) {
        // Забираем обычные ресурсы
        for (Map.Entry<Material, Integer> entry : research.getRequiredResources().entrySet()) {
            removeItems(player, entry.getKey(), entry.getValue());
        }
        
        // Забираем специальные ресурсы
        for (Map.Entry<String, Integer> entry : research.getSpecialResources().entrySet()) {
            removeSpecialItems(player, entry.getKey(), entry.getValue());
        }
        
        return true;
    }
    
    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                int toRemove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;
                
                if (item.getAmount() <= 0) {
                    player.getInventory().remove(item);
                }
            }
        }
    }
    
    private void removeSpecialItems(Player player, String itemType, int amount) {
        if ("uranium_block".equals(itemType)) {
            int remaining = amount;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && plugin.getUraniumItems().isUraniumItem(item) && remaining > 0) {
                    String type = plugin.getUraniumItems().getUraniumType(item);
                    if ("block".equals(type)) {
                        int toRemove = Math.min(item.getAmount(), remaining);
                        item.setAmount(item.getAmount() - toRemove);
                        remaining -= toRemove;
                        
                        if (item.getAmount() <= 0) {
                            player.getInventory().remove(item);
                        }
                    }
                }
            }
        }
    }
    
    private void showRequiredResources(Player player, AdvancedResearch research) {
        player.sendMessage("§c📋 Требуемые ресурсы:");
        
        for (Map.Entry<Material, Integer> entry : research.getRequiredResources().entrySet()) {
            int has = countItems(player, entry.getKey());
            int needed = entry.getValue();
            String status = has >= needed ? "§a✅" : "§c❌";
            
            player.sendMessage(String.format("%s §7%s: §f%d§7/§e%d", 
                status, getItemDisplayName(entry.getKey()), has, needed));
        }
        
        for (Map.Entry<String, Integer> entry : research.getSpecialResources().entrySet()) {
            int has = countSpecialItems(player, entry.getKey());
            int needed = entry.getValue();
            String status = has >= needed ? "§a✅" : "§c❌";
            
            player.sendMessage(String.format("%s §7%s: §f%d§7/§e%d", 
                status, getSpecialItemDisplayName(entry.getKey()), has, needed));
        }
    }
    
    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private int countSpecialItems(Player player, String itemType) {
        if ("uranium_block".equals(itemType)) {
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && plugin.getUraniumItems().isUraniumItem(item)) {
                    String type = plugin.getUraniumItems().getUraniumType(item);
                    if ("block".equals(type)) {
                        count += item.getAmount();
                    }
                }
            }
            return count;
        }
        return 0;
    }
    
    private String getItemDisplayName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }
    
    private String getSpecialItemDisplayName(String itemType) {
        switch (itemType) {
            case "uranium_block": return "Урановый блок";
            default: return itemType;
        }
    }
    
    private void updateActiveResearch() {
        for (Map.Entry<UUID, ActiveAdvancedResearch> entry : activeResearch.entrySet()) {
            ActiveAdvancedResearch research = entry.getValue();
            
            if (research.isComplete()) {
                UUID playerId = entry.getKey();
                Player player = plugin.getServer().getPlayer(playerId);
                
                if (player != null && player.isOnline()) {
                    completeResearch(player, research.getResearchId());
                }
                
                activeResearch.remove(playerId);
            }
        }
    }
    
    private void completeResearch(Player player, String researchId) {
        completedResearch.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(researchId);
        
        AdvancedResearch research = availableResearch.get(researchId);
        if (research != null) {
            player.sendMessage("§a🎉 Исследование '" + research.getName() + "' завершено!");
            player.sendMessage("§e💡 Результат доступен в терминале лаборатории!");
            
            // Звуковые эффекты
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            
            // Достижение
            plugin.getAchievementManager().unlockAchievement(player, "researcher_" + researchId);
        }
    }
    
    private String formatTime(long minutes) {
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d дн. %d ч.", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%d ч. %d мин.", hours, minutes % 60);
        } else {
            return String.format("%d мин.", minutes);
        }
    }
    
    public ActiveAdvancedResearch getActiveResearch(Player player) {
        return activeResearch.get(player.getUniqueId());
    }
    
    public boolean isResearchCompleted(Player player, String researchId) {
        Set<String> completed = completedResearch.get(player.getUniqueId());
        return completed != null && completed.contains(researchId);
    }
    
    public Map<String, AdvancedResearch> getAvailableResearch() {
        return availableResearch;
    }
    
    public Set<String> getCompletedResearch(Player player) {
        return completedResearch.getOrDefault(player.getUniqueId(), new HashSet<>());
    }
    
    public static class AdvancedResearch extends Research {
        private final Map<String, Integer> specialResources;
        
        public AdvancedResearch(String id, String name, String description, ResearchCategory category, int tier, long researchTimeMinutes) {
            super(id, name, description, category, tier, researchTimeMinutes);
            this.specialResources = new HashMap<>();
        }
        
        public AdvancedResearch addSpecialResource(String resourceType, int amount) {
            specialResources.put(resourceType, amount);
            return this;
        }
        
        public AdvancedResearch addRequiredResource(Material material, int amount) {
            getRequiredResources().put(material, amount);
            return this;
        }
        
        public Map<String, Integer> getSpecialResources() {
            return specialResources;
        }
    }
    
    public static class ActiveAdvancedResearch {
        private final String researchId;
        private final long startTime;
        private final long duration;
        
        public ActiveAdvancedResearch(String researchId, long startTime, long duration) {
            this.researchId = researchId;
            this.startTime = startTime;
            this.duration = duration;
        }
        
        public String getResearchId() { return researchId; }
        public long getStartTime() { return startTime; }
        public long getDuration() { return duration; }
        
        public double getProgress() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.min(1.0, (double) elapsed / duration);
        }
        
        public long getRemainingTime() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, duration - elapsed);
        }
        
        public boolean isComplete() {
            return System.currentTimeMillis() >= startTime + duration;
        }
    }
}
