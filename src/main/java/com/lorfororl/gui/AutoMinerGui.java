package com.lorfororl.gui;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.automation.AutoMiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class AutoMinerGui {
    
    private final LorForOrlPlugin plugin;
    private final AutoMiner autoMiner;
    
    public AutoMinerGui(LorForOrlPlugin plugin, AutoMiner autoMiner) {
        this.plugin = plugin;
        this.autoMiner = autoMiner;
    }
    
    public void openMainGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "⛏ Автошахтер - Управление");
        
        // Статус автошахтера
        ItemStack status = createGuiItem(
            autoMiner.isActive() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE,
            autoMiner.isActive() ? ChatColor.GREEN + "⚡ АКТИВЕН" : ChatColor.RED + "⚡ НЕАКТИВЕН",
            Arrays.asList(
                ChatColor.WHITE + "Энергия: " + autoMiner.getCurrentEnergy() + "/" + autoMiner.getEnergyCapacity(),
                ChatColor.WHITE + "Режим: " + autoMiner.getMode().getDisplayName(),
                ChatColor.WHITE + "Область: " + autoMiner.getRadius() + "x" + autoMiner.getDepth(),
                ChatColor.WHITE + "В очереди: " + autoMiner.getQueueSize() + " блоков",
                "",
                autoMiner.isActive() ? 
                    ChatColor.RED + "ЛКМ - Остановить" :
                    ChatColor.GREEN + "ЛКМ - Запустить"
            )
        );
        gui.setItem(4, status);
        
        // Настройки области
        ItemStack areaSettings = createGuiItem(Material.COMPASS, ChatColor.YELLOW + "📐 Настройки области",
            Arrays.asList(
                ChatColor.WHITE + "Радиус: " + autoMiner.getRadius() + " блоков",
                ChatColor.WHITE + "Глубина: " + autoMiner.getDepth() + " блоков",
                ChatColor.WHITE + "Общий объем: " + ((autoMiner.getRadius()*2+1) * (autoMiner.getRadius()*2+1) * autoMiner.getDepth()) + " блоков",
                "",
                ChatColor.YELLOW + "ЛКМ - Изменить радиус",
                ChatColor.YELLOW + "ПКМ - Изменить глубину",
                ChatColor.GRAY + "Shift+ЛКМ - Показать область"
            ));
        gui.setItem(20, areaSettings);
        
        // Режим добычи
        ItemStack modeSettings = createGuiItem(Material.DIAMOND_PICKAXE, ChatColor.AQUA + "⚙ Режим добычи",
            Arrays.asList(
                ChatColor.WHITE + "Текущий: " + autoMiner.getMode().getDisplayName(),
                "",
                ChatColor.GRAY + "Доступные режимы:",
                ChatColor.WHITE + "• Выборочная добыча",
                ChatColor.WHITE + "• Только руды",
                ChatColor.WHITE + "• Все блоки",
                "",
                ChatColor.YELLOW + "ЛКМ - Переключить режим"
            ));
        gui.setItem(22, modeSettings);
        
        // Фильтры блоков
        ItemStack blockFilters = createGuiItem(Material.HOPPER, ChatColor.GOLD + "🔍 Фильтры блоков",
            Arrays.asList(
                ChatColor.WHITE + "Разрешено: " + autoMiner.getAllowedBlocks().size() + " типов",
                ChatColor.WHITE + "Запрещено: " + autoMiner.getBlacklistedBlocks().size() + " типов",
                "",
                ChatColor.YELLOW + "ЛКМ - Настроить фильтры"
            ));
        gui.setItem(24, blockFilters);
        
        // Статистика
        ItemStack statistics = createGuiItem(Material.BOOK, ChatColor.GREEN + "📊 Статистика",
            Arrays.asList(
                ChatColor.WHITE + "Добыто сегодня: " + autoMiner.getBlocksMinedToday(),
                ChatColor.WHITE + "Всего добыто: " + autoMiner.getTotalBlocksMined(),
                ChatColor.WHITE + "Время работы: " + formatTime(autoMiner.getUptime()),
                "",
                ChatColor.YELLOW + "ЛКМ - Подробная статистика"
            ));
        gui.setItem(40, statistics);
        
        // Энергия
        double energyPercent = (double) autoMiner.getCurrentEnergy() / autoMiner.getEnergyCapacity() * 100;
        ItemStack energy = createGuiItem(Material.REDSTONE_BLOCK, ChatColor.RED + "⚡ Энергия",
            Arrays.asList(
                ChatColor.WHITE + String.format("Заряд: %d/%d (%.1f%%)", 
                    autoMiner.getCurrentEnergy(), autoMiner.getEnergyCapacity(), energyPercent),
                ChatColor.WHITE + "Расход: " + autoMiner.getEnergyRequired() + " ед/блок",
                "",
                energyPercent > 50 ? ChatColor.GREEN + "Энергии достаточно" :
                energyPercent > 20 ? ChatColor.YELLOW + "Энергия на исходе" :
                ChatColor.RED + "Критически мало энергии!",
                "",
                ChatColor.YELLOW + "ЛКМ - Подключить источник энергии"
            ));
        gui.setItem(13, energy);
        
        // Обслуживание
        ItemStack maintenance = createGuiItem(Material.ANVIL, ChatColor.DARK_PURPLE + "🔧 Обслуживание",
            Arrays.asList(
                ChatColor.WHITE + "Состояние: " + ChatColor.GREEN + "Отличное",
                ChatColor.WHITE + "Износ: 0%",
                ChatColor.WHITE + "Эффективность: 100%",
                "",
                ChatColor.YELLOW + "ЛКМ - Диагностика",
                ChatColor.YELLOW + "ПКМ - Ремонт"
            ));
        gui.setItem(31, maintenance);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }
    
    public void openStatisticsGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "📊 Статистика автошахтера");
        
        // Общая статистика
        ItemStack generalStats = createGuiItem(Material.PAPER, ChatColor.YELLOW + "📈 Общая статистика",
            Arrays.asList(
                ChatColor.WHITE + "Всего добыто: " + autoMiner.getTotalBlocksMined() + " блоков",
                ChatColor.WHITE + "Добыто сегодня: " + autoMiner.getBlocksMinedToday() + " блоков",
                ChatColor.WHITE + "Время работы: " + formatTime(autoMiner.getUptime()),
                ChatColor.WHITE + "Средняя скорость: " + calculateAverageSpeed() + " блоков/час",
                "",
                ChatColor.GREEN + "Эффективность: " + calculateEfficiency() + "%"
            ));
        gui.setItem(4, generalStats);
        
        // Добытые ресурсы
        int slot = 18;
        for (Map.Entry<Material, Integer> entry : autoMiner.getMinedResources().entrySet()) {
            if (slot >= 35) break;
            
            Material material = entry.getKey();
            int count = entry.getValue();
            
            ItemStack resourceItem = createGuiItem(material, 
                ChatColor.WHITE + getItemDisplayName(material),
                Arrays.asList(
                    ChatColor.YELLOW + "Добыто: " + count + " блоков",
                    ChatColor.GRAY + "Процент от общего: " + String.format("%.1f%%", 
                        (double) count / autoMiner.getTotalBlocksMined() * 100)
                ));
            
            gui.setItem(slot++, resourceItem);
        }
        
        // Кнопка возврата
        ItemStack backButton = createGuiItem(Material.ARROW, ChatColor.GRAY + "← Назад",
            Arrays.asList(ChatColor.YELLOW + "Вернуться к главному меню"));
        gui.setItem(49, backButton);
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    public void openFilterGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "🔍 Фильтры блоков");
        
        // Режимы фильтрации
        ItemStack selectiveMode = createGuiItem(
            autoMiner.getMode() == AutoMiner.MiningMode.SELECTIVE ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            ChatColor.GREEN + "Выборочная добыча",
            Arrays.asList(
                ChatColor.WHITE + "Добывает только разрешенные блоки",
                ChatColor.GRAY + "Настраивается через белый список",
                "",
                autoMiner.getMode() == AutoMiner.MiningMode.SELECTIVE ? 
                    ChatColor.GREEN + "✓ Активен" : ChatColor.YELLOW + "ЛКМ - Активировать"
            ));
        gui.setItem(10, selectiveMode);
        
        ItemStack oresMode = createGuiItem(
            autoMiner.getMode() == AutoMiner.MiningMode.ORES_ONLY ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            ChatColor.GOLD + "Только руды",
            Arrays.asList(
                ChatColor.WHITE + "Добывает только руды",
                ChatColor.GRAY + "Автоматически определяет руды",
                "",
                autoMiner.getMode() == AutoMiner.MiningMode.ORES_ONLY ? 
                    ChatColor.GREEN + "✓ Активен" : ChatColor.YELLOW + "ЛКМ - Активировать"
            ));
        gui.setItem(12, oresMode);
        
        ItemStack allMode = createGuiItem(
            autoMiner.getMode() == AutoMiner.MiningMode.ALL_BLOCKS ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            ChatColor.RED + "Все блоки",
            Arrays.asList(
                ChatColor.WHITE + "Добывает все твердые блоки",
                ChatColor.RED + "⚠ Может повредить структуры!",
                "",
                autoMiner.getMode() == AutoMiner.MiningMode.ALL_BLOCKS ? 
                    ChatColor.GREEN + "✓ Активен" : ChatColor.YELLOW + "ЛКМ - Активировать"
            ));
        gui.setItem(14, allMode);
        
        // Управление списками
        ItemStack whitelistManager = createGuiItem(Material.WHITE_WOOL, ChatColor.GREEN + "✅ Белый список",
            Arrays.asList(
                ChatColor.WHITE + "Разрешенные блоки: " + autoMiner.getAllowedBlocks().size(),
                "",
                ChatColor.YELLOW + "ЛКМ - Управление списком"
            ));
        gui.setItem(30, whitelistManager);
        
        ItemStack blacklistManager = createGuiItem(Material.BLACK_WOOL, ChatColor.RED + "❌ Черный список",
            Arrays.asList(
                ChatColor.WHITE + "Запрещенные блоки: " + autoMiner.getBlacklistedBlocks().size(),
                "",
                ChatColor.YELLOW + "ЛКМ - Управление списком"
            ));
        gui.setItem(32, blacklistManager);
        
        // Кнопка возврата
        ItemStack backButton = createGuiItem(Material.ARROW, ChatColor.GRAY + "← Назад",
            Arrays.asList(ChatColor.YELLOW + "Вернуться к главному меню"));
        gui.setItem(49, backButton);
        
        fillEmptySlots(gui);
        player.openInventory(gui);
    }
    
    private ItemStack createGuiItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds % 60);
        } else {
            return String.format("%dс", seconds);
        }
    }
    
    private String getItemDisplayName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }
    
    private double calculateAverageSpeed() {
        long uptime = autoMiner.getUptime();
        if (uptime == 0) return 0;
        
        double hours = uptime / (1000.0 * 60 * 60);
        return autoMiner.getTotalBlocksMined() / Math.max(hours, 0.01);
    }
    
    private int calculateEfficiency() {
        // Простая формула эффективности на основе времени работы и добытых блоков
        long uptime = autoMiner.getUptime();
        if (uptime == 0) return 100;
        
        double expectedBlocks = (uptime / 1000.0) / 5.0; // Ожидаем 1 блок каждые 5 секунд
        double actualBlocks = autoMiner.getTotalBlocksMined();
        
        return (int) Math.min(100, (actualBlocks / Math.max(expectedBlocks, 1)) * 100);
    }
}
