package com.lorfororl.gui;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.Component;

public class AnimatedGuiManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, AnimatedGui> activeGuis;
    private BukkitTask cleanupTask;
    
    public AnimatedGuiManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeGuis = new ConcurrentHashMap<>();
        startCleanupTask();
    }
    
    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupInactiveGuis();
            }
        }.runTaskTimer(plugin, 0L, 100L); // Каждые 5 секунд
    }
    
    private void cleanupInactiveGuis() {
        Iterator<Map.Entry<UUID, AnimatedGui>> iterator = activeGuis.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, AnimatedGui> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            AnimatedGui gui = entry.getValue();
            
            if (player == null || !player.isOnline() || 
                !player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
                gui.forceClose();
                iterator.remove();
            }
        }
    }
    
    public void openLaboratoryGui(Player player) {
        closeExistingGui(player);
        
        try {
            AnimatedGui gui = new AnimatedGui(player, 54, 
                "§8§l◆═══════════════════════════════════════◆\n" +
                "§b§l            🧪 ЛАБОРАТОРИЯ 🧪\n" +
                "§8§l◆═══════════════════════════════════════◆");
            
            // Безопасная анимированная рамка
            gui.addAnimatedBorder();
            
            // Статус лаборатории с проверками
            if (plugin.getEnergyManager() != null && plugin.getResearchManager() != null) {
                int energy = plugin.getEnergyManager().getPlayerEnergy(player);
                int completedResearch = plugin.getResearchManager().getCompletedResearch(player).size();
                
                gui.setAnimatedItem(4, createPulsingItem(Material.BEACON, 
                    "§b§l◆ СТАТУС ЛАБОРАТОРИИ ◆",
                    Arrays.asList(
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "§aСостояние: §2●§a АКТИВНА",
                        "§eЭнергия: " + createProgressBar(energy, 1000, "§a", "§7"),
                        "§bИсследований: §3" + completedResearch + "§7/§312",
                        "§dТемпература: §523°C",
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "",
                        "§7» §eЛКМ §7- Обновить статус",
                        "§7» §eПКМ §7- Диагностика систем"
                    )));
            }
            
            // Текущее исследование с проверками
            if (plugin.getResearchManager() != null) {
                var activeResearch = plugin.getResearchManager().getActiveResearch(player);
                if (activeResearch != null) {
                    gui.setAnimatedItem(13, createGlowingItem(Material.ENCHANTED_BOOK,
                        "§e§l📚 ТЕКУЩЕЕ ИССЛЕДОВАНИЕ",
                        Arrays.asList(
                            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "§fНазвание: §6" + activeResearch.getResearchId(),
                            "§fПрогресс: " + createProgressBar((int)(activeResearch.getProgress() * 100), 100, "§a", "§7"),
                            "§fВремя: §b" + formatTime(activeResearch.getRemainingTime()),
                            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "",
                            activeResearch.isComplete() ? 
                                "§a§l✓ ГОТОВО К ЗАВЕРШЕНИЮ!" :
                                "§e⏳ Исследование в процессе...",
                            "",
                            "§7» §eЛКМ §7- " + (activeResearch.isComplete() ? "Завершить" : "Подробности")
                        )));
                } else {
                    gui.setAnimatedItem(13, createFloatingItem(Material.WRITABLE_BOOK,
                        "§7§l📖 НЕТ АКТИВНОГО ИССЛЕДОВАНИЯ",
                        Arrays.asList(
                            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "§fВыберите исследование для начала",
                            "§fработы в лаборатории",
                            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                            "",
                            "§7» §eЛКМ §7- Открыть каталог исследований"
                        )));
                }
            }
            
            // Остальные элементы GUI...
            gui.setAnimatedItem(20, createSparklingItem(Material.KNOWLEDGE_BOOK,
                "§3§l🔬 КАТАЛОГ ИССЛЕДОВАНИЙ",
                Arrays.asList(
                    "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "§fДоступно исследований: §a" + getAvailableResearchCount(player),
                    "§fТребует ресурсов: §c" + getPendingResourcesCount(player),
                    "§fЗаблокировано: §8" + getLockedResearchCount(player),
                    "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "",
                    "§7» §eЛКМ §7- Просмотреть каталог",
                    "§7» §eПКМ §7- Фильтры поиска"
                )));
            
            gui.open();
            activeGuis.put(player.getUniqueId(), gui);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка открытия GUI лаборатории для " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cОшибка открытия интерфейса лаборатории!");
        }
    }
    
    public void openReactorGui(Player player, org.bukkit.Location reactorLocation) {
        closeExistingGui(player);
        
        try {
            if (plugin.getReactorManager() == null) {
                player.sendMessage("§cСистема реакторов недоступна!");
                return;
            }
            
            var reactor = plugin.getReactorManager().getReactorAt(reactorLocation);
            if (reactor == null) {
                player.sendMessage("§cРеактор не найден в данной локации!");
                return;
            }
            
            AnimatedGui gui = new AnimatedGui(player, 54, 
                "§8§l◆═══════════════════════════════════════◆\n" +
                "§4§l          ☢ ЯДЕРНЫЙ РЕАКТОР ☢\n" +
                "§8§l◆═══════════════════════════════════════◆");
            
            // Анимированная рамка с предупреждением
            gui.addDangerBorder();
            
            // Статус реактора с проверками
            String statusColor = reactor.isActive() ? "§a" : "§c";
            String statusText = reactor.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН";
            if (reactor.isOverheating()) {
                statusColor = "§4";
                statusText = "ПЕРЕГРЕВ";
            }
            
            gui.setAnimatedItem(4, reactor.isOverheating() ? 
                createDangerItem(Material.BEACON, "§4§l⚠ КРИТИЧЕСКОЕ СОСТОЯНИЕ ⚠", 
                    Arrays.asList(
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "§cСостояние: " + statusColor + "●§c " + statusText,
                        "§6Температура: §4" + reactor.getTemperature() + "°C §c[КРИТИЧНО]",
                        "§9Давление: §1" + calculatePressure(reactor) + " атм",
                        "§aВыработка: §2" + reactor.getEnergyOutput() + "§7/§2" + reactor.getMaxEnergyOutput() + " ед/тик",
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "",
                        "§4§l⚠ НЕМЕДЛЕННО ВЫКЛЮЧИТЕ РЕАКТОР! ⚠",
                        "",
                        "§7» §cЛКМ §7- АВАРИЙНОЕ ОТКЛЮЧЕНИЕ"
                    )) :
                createPulsingItem(Material.BEACON, "§e§l⚙ СТАТУС РЕАКТОРА ⚙",
                    Arrays.asList(
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "§cСостояние: " + statusColor + "●§c " + statusText,
                        "§6Температура: " + getTempColor(reactor.getTemperature()) + reactor.getTemperature() + "°C",
                        "§9Давление: §1" + calculatePressure(reactor) + " атм",
                        "§aВыработка: §2" + reactor.getEnergyOutput() + "§7/§2" + reactor.getMaxEnergyOutput() + " ед/тик",
                        "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                        "",
                        reactor.isActive() ? 
                            "§7» §cЛКМ §7- Выключить реактор" :
                            "§7» §aЛКМ §7- Включить реактор"
                    )));
            
            gui.open();
            activeGuis.put(player.getUniqueId(), gui);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка открытия GUI реактора для " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cОшибка открытия интерфейса реактора!");
        }
    }
    
    private void closeExistingGui(Player player) {
        AnimatedGui existingGui = activeGuis.remove(player.getUniqueId());
        if (existingGui != null) {
            existingGui.forceClose();
        }
    }
    
    // Безопасные методы создания предметов
    private ItemStack createPulsingItem(Material material, String name, List<String> lore) {
        return createAnimatedItem(material, name, lore, "pulse");
    }
    
    private ItemStack createGlowingItem(Material material, String name, List<String> lore) {
        return createAnimatedItem(material, name, lore, "glow");
    }
    
    private ItemStack createSparklingItem(Material material, String name, List<String> lore) {
        return createAnimatedItem(material, name, lore, "sparkle");
    }
    
    private ItemStack createFloatingItem(Material material, String name, List<String> lore) {
        return createAnimatedItem(material, name, lore, "float");
    }
    
    private ItemStack createDangerItem(Material material, String name, List<String> lore) {
        return createAnimatedItem(material, name, lore, "danger");
    }
    
    private ItemStack createAnimatedItem(Material material, String name, List<String> lore, String effect) {
        try {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка создания анимированного предмета: " + e.getMessage());
            return new ItemStack(Material.BARRIER);
        }
    }
    
    private String createProgressBar(int current, int max, String filledColor, String emptyColor) {
        if (max <= 0) return "§cОшибка";
        
        int bars = 20;
        int filled = Math.min(bars, (int) ((double) current / max * bars));
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append(filledColor).append("█");
            } else {
                bar.append(emptyColor).append("█");
            }
        }
        bar.append(" §f").append(current).append("§7/§f").append(max);
        
        return bar.toString();
    }
    
    private String formatTime(long milliseconds) {
        if (milliseconds < 0) return "00:00";
        
        long minutes = milliseconds / (1000 * 60);
        long seconds = (milliseconds % (1000 * 60)) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Безопасные вспомогательные методы
    private int getAvailableResearchCount(Player player) {
        try {
            if (plugin.getResearchManager() != null) {
                return 5; // Заглушка
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка получения количества исследований: " + e.getMessage());
        }
        return 0;
    }
    
    private int getPendingResourcesCount(Player player) {
        return 3; // Заглушка
    }
    
    private int getLockedResearchCount(Player player) {
        return 5; // Заглушка
    }
    
    private int calculatePressure(com.lorfororl.reactor.NuclearReactor reactor) {
        try {
            return Math.max(1, reactor.getTemperature() / 100);
        } catch (Exception e) {
            return 1;
        }
    }
    
    private String getTempColor(int temperature) {
        if (temperature > 900) return "§4";
        if (temperature > 800) return "§c";
        if (temperature > 600) return "§6";
        if (temperature > 400) return "§e";
        return "§a";
    }
    
    public void closeGui(Player player) {
        AnimatedGui gui = activeGuis.remove(player.getUniqueId());
        if (gui != null) {
            gui.close();
        }
    }
    
    public void shutdown() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel();
        }
        
        // Безопасно закрываем все GUI
        for (AnimatedGui gui : activeGuis.values()) {
            try {
                gui.forceClose();
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка закрытия GUI: " + e.getMessage());
            }
        }
        activeGuis.clear();
    }
    
    // Улучшенный класс AnimatedGui с защитой от утечек памяти
    private class AnimatedGui {
        private final Player player;
        private final Inventory inventory;
        private final Map<Integer, BukkitTask> animations;
        private BukkitTask borderAnimation;
        private boolean closed = false;
        
        public AnimatedGui(Player player, int size, String title) {
            this.player = player;
            this.inventory = Bukkit.createInventory(null, size, title);
            this.animations = new ConcurrentHashMap<>();
        }
        
        public void addAnimatedBorder() {
            if (closed) return;
            
            borderAnimation = new BukkitRunnable() {
                private int tick = 0;
                private final Material[] borderMaterials = {
                    Material.BLUE_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    Material.CYAN_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE
                };
                
                @Override
                public void run() {
                    if (closed || !player.isOnline() || 
                        !player.getOpenInventory().getTopInventory().equals(inventory)) {
                        cancel();
                        return;
                    }
                    
                    try {
                        Material currentMaterial = borderMaterials[tick % borderMaterials.length];
                        
                        // Безопасное обновление границ
                        updateBorders(currentMaterial);
                        
                        tick++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ошибка анимации границы: " + e.getMessage());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }
        
        public void addDangerBorder() {
            if (closed) return;
            
            borderAnimation = new BukkitRunnable() {
                private int tick = 0;
                private final Material[] dangerMaterials = {
                    Material.RED_STAINED_GLASS_PANE,
                    Material.ORANGE_STAINED_GLASS_PANE,
                    Material.YELLOW_STAINED_GLASS_PANE,
                    Material.ORANGE_STAINED_GLASS_PANE
                };
                
                @Override
                public void run() {
                    if (closed || !player.isOnline() || 
                        !player.getOpenInventory().getTopInventory().equals(inventory)) {
                        cancel();
                        return;
                    }
                    
                    try {
                        Material currentMaterial = dangerMaterials[tick % dangerMaterials.length];
                        updateBorders(currentMaterial);
                        
                        // Звуковые эффекты для опасности
                        if (tick % 40 == 0) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
                        }
                        
                        tick++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ошибка анимации опасной границы: " + e.getMessage());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 5L);
        }
        
        private void updateBorders(Material material) {
            ItemStack borderItem = createBorderItem(material);
            
            // Верхняя и нижняя границы
            for (int i = 0; i < 9; i++) {
                if (shouldUpdateSlot(i)) {
                    inventory.setItem(i, borderItem);
                }
                if (shouldUpdateSlot(inventory.getSize() - 9 + i)) {
                    inventory.setItem(inventory.getSize() - 9 + i, borderItem);
                }
            }
            
            // Боковые границы
            for (int row = 1; row < inventory.getSize() / 9 - 1; row++) {
                int leftSlot = row * 9;
                int rightSlot = row * 9 + 8;
                
                if (shouldUpdateSlot(leftSlot)) {
                    inventory.setItem(leftSlot, borderItem);
                }
                if (shouldUpdateSlot(rightSlot)) {
                    inventory.setItem(rightSlot, borderItem);
                }
            }
        }
        
        private boolean shouldUpdateSlot(int slot) {
            ItemStack current = inventory.getItem(slot);
            return current == null || current.getType().name().contains("GLASS_PANE");
        }
        
        private ItemStack createBorderItem(Material material) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(" ");
                item.setItemMeta(meta);
            }
            return item;
        }
        
        public void setAnimatedItem(int slot, ItemStack item) {
            if (closed) return;
            
            inventory.setItem(slot, item);
            
            // Останавливаем предыдущую анимацию для этого слота
            BukkitTask oldAnimation = animations.remove(slot);
            if (oldAnimation != null && !oldAnimation.isCancelled()) {
                oldAnimation.cancel();
            }
            
            // Добавляем новую анимацию
            BukkitTask animation = new BukkitRunnable() {
                private int tick = 0;
                
                @Override
                public void run() {
                    if (closed || !player.isOnline() || 
                        !player.getOpenInventory().getTopInventory().equals(inventory)) {
                        cancel();
                        return;
                    }
                    
                    try {
                        // Простая анимация количества для эффекта пульсации
                        int amount = (int) (Math.sin(tick * 0.2) * 16 + 17);
                        amount = Math.max(1, Math.min(64, amount));
                        
                        ItemStack animatedItem = item.clone();
                        animatedItem.setAmount(amount);
                        inventory.setItem(slot, animatedItem);
                        
                        tick++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ошибка анимации предмета: " + e.getMessage());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 3L);
            
            animations.put(slot, animation);
        }
        
        public void open() {
            if (closed) return;
            
            try {
                player.openInventory(inventory);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.2f);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка открытия GUI: " + e.getMessage());
            }
        }
        
        public void close() {
            forceClose();
            try {
                player.closeInventory();
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка закрытия GUI: " + e.getMessage());
            }
        }
        
        public void forceClose() {
            closed = true;
            
            if (borderAnimation != null && !borderAnimation.isCancelled()) {
                borderAnimation.cancel();
            }
            
            for (BukkitTask animation : animations.values()) {
                if (animation != null && !animation.isCancelled()) {
                    animation.cancel();
                }
            }
            animations.clear();
        }
        
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void sendActionBar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }
}
