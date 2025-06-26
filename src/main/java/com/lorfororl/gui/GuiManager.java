package com.lorfororl.gui;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, String> openGuis;
    
    public GuiManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
    }
    
    public void openLaboratoryGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Лаборатория");
        
        // Статус лаборатории
        ItemStack status = createGuiItem(Material.BEACON, ChatColor.GREEN + "Статус лаборатории",
            Arrays.asList(
                ChatColor.WHITE + "Состояние: " + ChatColor.GREEN + "АКТИВНА",
                ChatColor.WHITE + "Энергия: " + plugin.getEnergyManager().getPlayerEnergy(player) + "/1000",
                ChatColor.WHITE + "Исследований завершено: " + plugin.getResearchManager().getCompletedResearch(player).size(),
                "",
                ChatColor.YELLOW + "ЛКМ - Обновить статус"
            ));
        gui.setItem(4, status);
        
        // Текущее исследование
        var activeResearch = plugin.getResearchManager().getActiveResearch(player);
        ItemStack research;
        if (activeResearch != null) {
            double progress = activeResearch.getProgress() * 100;
            long remainingMinutes = activeResearch.getRemainingTime() / (1000 * 60);
            
            research = createGuiItem(Material.BOOK, ChatColor.YELLOW + "Текущее исследование",
                Arrays.asList(
                    ChatColor.WHITE + "Название: " + activeResearch.getResearchId(),
                    ChatColor.WHITE + String.format("Прогресс: %.1f%%", progress),
                    ChatColor.WHITE + String.format("Осталось: %d мин", remainingMinutes),
                    "",
                    activeResearch.isComplete() ? 
                        ChatColor.GREEN + "ЛКМ - Завершить исследование" :
                        ChatColor.GRAY + "Исследование в процессе..."
                ));
        } else {
            research = createGuiItem(Material.WRITABLE_BOOK, ChatColor.GRAY + "Нет активного исследования",
                Arrays.asList(
                    ChatColor.WHITE + "Выберите исследование для начала",
                    "",
                    ChatColor.YELLOW + "ЛКМ - Открыть список исследований"
                ));
        }
        gui.setItem(13, research);
        
        // Доступные исследования
        ItemStack availableResearch = createGuiItem(Material.ENCHANTED_BOOK, ChatColor.AQUA + "Доступные исследования",
            Arrays.asList(
                ChatColor.WHITE + "Просмотр всех доступных исследований",
                ChatColor.WHITE + "и их требований",
                "",
                ChatColor.YELLOW + "ЛКМ - Открыть список"
            ));
        gui.setItem(20, availableResearch);
        
        // Завершенные исследования
        ItemStack completedResearch = createGuiItem(Material.KNOWLEDGE_BOOK, ChatColor.GREEN + "Завершенные исследования",
            Arrays.asList(
                ChatColor.WHITE + "Количество: " + plugin.getResearchManager().getCompletedResearch(player).size(),
                "",
                ChatColor.YELLOW + "ЛКМ - Просмотреть список"
            ));
        gui.setItem(24, completedResearch);
        
        // Ускорение исследований
        ItemStack acceleration = createGuiItem(Material.REDSTONE, ChatColor.RED + "Ускорение исследований",
            Arrays.asList(
                ChatColor.WHITE + "Текущий бонус: +0%",
                ChatColor.WHITE + "Энергия: " + (plugin.getEnergyManager().getPlayerEnergy(player) > 100 ? "+25%" : "0%"),
                ChatColor.WHITE + "Исследовательская станция: +50%",
                "",
                ChatColor.YELLOW + "Требует энергию для ускорения"
            ));
        gui.setItem(40, acceleration);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "laboratory");
    }
    
    public void openReactorGui(Player player, org.bukkit.Location reactorLocation) {
        var reactor = plugin.getReactorManager().getReactorAt(reactorLocation);
        if (reactor == null) return;
        
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Ядерный реактор");
        
        // Статус реактора
        String statusText = reactor.isActive() ? ChatColor.GREEN + "АКТИВЕН" : ChatColor.RED + "НЕАКТИВЕН";
        if (reactor.isOverheating()) statusText += ChatColor.RED + " [ПЕРЕГРЕВ]";
        
        ItemStack status = createGuiItem(Material.BEACON, ChatColor.YELLOW + "Статус реактора",
            Arrays.asList(
                ChatColor.WHITE + "Состояние: " + statusText,
                ChatColor.WHITE + String.format("Температура: %d°C", reactor.getTemperature()),
                ChatColor.WHITE + String.format("Выработка: %d/%d ед/тик", reactor.getEnergyOutput(), reactor.getMaxEnergyOutput()),
                "",
                reactor.isActive() ? 
                    ChatColor.RED + "ЛКМ - Выключить реактор" :
                    ChatColor.GREEN + "ЛКМ - Включить реактор"
            ));
        gui.setItem(4, status);
        
        // Топливо
        ItemStack fuel = createGuiItem(Material.COAL, ChatColor.GOLD + "Топливо",
            Arrays.asList(
                ChatColor.WHITE + String.format("Уровень: %d/64", reactor.getFuelLevel()),
                ChatColor.WHITE + "Тип: Урановые стержни",
                ChatColor.WHITE + String.format("Расход: 1 стержень/5 мин"),
                "",
                ChatColor.YELLOW + "ПКМ - Добавить топливо",
                ChatColor.GRAY + "Требует: Урановые блоки"
            ));
        gui.setItem(20, fuel);
        
        // Охлаждение
        boolean hasWaterCooling = checkWaterCooling(reactorLocation);
        ItemStack cooling = createGuiItem(Material.WATER_BUCKET, ChatColor.BLUE + "Система охлаждения",
            Arrays.asList(
                ChatColor.WHITE + "Водяное охлаждение: " + (hasWaterCooling ? ChatColor.GREEN + "АКТИВНО" : ChatColor.RED + "НЕАКТИВНО"),
                ChatColor.WHITE + String.format("Эффективность: %d%%", hasWaterCooling ? 100 : 50),
                ChatColor.WHITE + String.format("Критическая температура: 950°C"),
                "",
                ChatColor.YELLOW + "Разместите воду вокруг реактора",
                ChatColor.YELLOW + "для улучшения охлаждения"
            ));
        gui.setItem(22, cooling);
        
        // Безопасность
        ItemStack safety = createGuiItem(Material.REDSTONE_LAMP, ChatColor.RED + "Система безопасности",
            Arrays.asList(
                ChatColor.WHITE + "Аварийное отключение: " + ChatColor.GREEN + "ГОТОВО",
                ChatColor.WHITE + "Радиационная защита: " + ChatColor.GREEN + "АКТИВНА",
                ChatColor.WHITE + "Автоматическое охлаждение: " + (hasWaterCooling ? ChatColor.GREEN + "АКТИВНО" : ChatColor.RED + "НЕАКТИВНО"),
                "",
                reactor.getTemperature() > 800 ? 
                    ChatColor.RED + "⚠ ВНИМАНИЕ: ПЕРЕГРЕВ!" :
                    ChatColor.GREEN + "Все системы в норме"
            ));
        gui.setItem(24, safety);
        
        // Энергия
        ItemStack energy = createGuiItem(Material.LIGHTNING_ROD, ChatColor.YELLOW + "Энергетический выход",
            Arrays.asList(
                ChatColor.WHITE + String.format("Текущая выработка: %d ед/тик", reactor.getEnergyOutput()),
                ChatColor.WHITE + String.format("Максимальная выработка: %d ед/тик", reactor.getMaxEnergyOutput()),
                ChatColor.WHITE + String.format("Эффективность: %d%%", reactor.getEnergyOutput() * 100 / Math.max(1, reactor.getMaxEnergyOutput())),
                "",
                ChatColor.YELLOW + "Подключите энергопотребители"
            ));
        gui.setItem(40, energy);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "reactor:" + reactorLocation.toString());
    }
    
    public void openGeneratorGui(Player player, org.bukkit.Location generatorLocation) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Энергогенератор");
        
        // Статус генератора
        ItemStack status = createGuiItem(Material.LIGHTNING_ROD, ChatColor.YELLOW + "Статус генератора",
            Arrays.asList(
                ChatColor.WHITE + "Состояние: " + ChatColor.GREEN + "АКТИВЕН",
                ChatColor.WHITE + "Выработка: 0-500 ед/удар молнии",
                ChatColor.WHITE + "Накоплено: 0/10000 ед",
                "",
                ChatColor.YELLOW + "Ожидает грозы..."
            ));
        gui.setItem(4, status);
        
        // Громоотводы
        ItemStack rods = createGuiItem(Material.COPPER_INGOT, ChatColor.GOLD + "Громоотводы",
            Arrays.asList(
                ChatColor.WHITE + "Установлено: 5/8",
                ChatColor.WHITE + "Эффективность: 62%",
                ChatColor.WHITE + "Радиус действия: 32 блока",
                "",
                ChatColor.YELLOW + "Добавьте больше громоотводов",
                ChatColor.YELLOW + "для увеличения эффективности"
            ));
        gui.setItem(20, rods);
        
        // Накопители энергии
        ItemStack storage = createGuiItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Накопители энергии",
            Arrays.asList(
                ChatColor.WHITE + "Емкость: 10000 ед",
                ChatColor.WHITE + "Заполнено: 0 ед (0%)",
                ChatColor.WHITE + "Скорость разряда: 50 ед/тик",
                "",
                ChatColor.YELLOW + "ПКМ - Извлечь энергию"
            ));
        gui.setItem(24, storage);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "generator:" + generatorLocation.toString());
    }
    
    public void openSolarPanelGui(Player player, org.bukkit.Location panelLocation) {
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.YELLOW + "Солнечная панель");
        
        // Статус панели
        boolean isDaytime = player.getWorld().getTime() < 12300 || player.getWorld().getTime() > 23850;
        boolean hasDirectSunlight = player.getWorld().getHighestBlockYAt(panelLocation) <= panelLocation.getBlockY();
        
        ItemStack status = createGuiItem(Material.DAYLIGHT_DETECTOR, ChatColor.YELLOW + "Статус панели",
            Arrays.asList(
                ChatColor.WHITE + "Время суток: " + (isDaytime ? ChatColor.GREEN + "ДЕНЬ" : ChatColor.GRAY + "НОЧЬ"),
                ChatColor.WHITE + "Прямой солнечный свет: " + (hasDirectSunlight ? ChatColor.GREEN + "ДА" : ChatColor.RED + "НЕТ"),
                ChatColor.WHITE + "Выработка: " + (isDaytime && hasDirectSunlight ? "25 ед/тик" : "0 ед/тик"),
                ChatColor.WHITE + "Эффективность: " + (isDaytime && hasDirectSunlight ? "100%" : "0%"),
                "",
                !hasDirectSunlight ? ChatColor.RED + "⚠ Панель затенена!" : ChatColor.GREEN + "Оптимальные условия"
            ));
        gui.setItem(4, status);
        
        // Накопленная энергия
        ItemStack energy = createGuiItem(Material.REDSTONE, ChatColor.RED + "Накопленная энергия",
            Arrays.asList(
                ChatColor.WHITE + "Текущий заряд: 0/1000 ед",
                ChatColor.WHITE + "Выработка за день: 0 ед",
                ChatColor.WHITE + "Общая выработка: 0 ед",
                "",
                ChatColor.YELLOW + "ПКМ - Извлечь энергию"
            ));
        gui.setItem(13, energy);
        
        // Обслуживание
        ItemStack maintenance = createGuiItem(Material.BRUSH, ChatColor.AQUA + "Обслуживание",
            Arrays.asList(
                ChatColor.WHITE + "Чистота панели: 100%",
                ChatColor.WHITE + "Износ: 0%",
                ChatColor.WHITE + "Следующее ТО: через 7 дней",
                "",
                ChatColor.YELLOW + "ЛКМ - Очистить панель",
                ChatColor.GRAY + "Грязные панели менее эффективны"
            ));
        gui.setItem(22, maintenance);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "solar:" + panelLocation.toString());
    }
    
    public void openResearchStationGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Исследовательская станция");
        
        // Статус станции
        ItemStack status = createGuiItem(Material.LECTERN, ChatColor.LIGHT_PURPLE + "Статус станции",
            Arrays.asList(
                ChatColor.WHITE + "Состояние: " + ChatColor.GREEN + "АКТИВНА",
                ChatColor.WHITE + "Бонус к скорости: +50%",
                ChatColor.WHITE + "Энергопотребление: 10 ед/тик",
                "",
                ChatColor.YELLOW + "Ускоряет все исследования"
            ));
        gui.setItem(4, status);
        
        // Доступные исследования по уровням
        for (int tier = 1; tier <= 5; tier++) {
            ItemStack tierItem = createGuiItem(
                Material.COPPER_INGOT, // Можно заменить на разные материалы для каждого уровня
                ChatColor.GOLD + "Уровень " + tier,
                Arrays.asList(
                    ChatColor.WHITE + "Исследования " + tier + " уровня",
                    ChatColor.WHITE + "Сложность: " + "★".repeat(tier),
                    "",
                    ChatColor.YELLOW + "ЛКМ - Просмотреть исследования"
                )
            );
            gui.setItem(18 + tier, tierItem);
        }
        
        // Прогресс исследований
        var completed = plugin.getResearchManager().getCompletedResearch(player);
        ItemStack progress = createGuiItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Прогресс исследований",
            Arrays.asList(
                ChatColor.WHITE + "Завершено исследований: " + completed.size(),
                ChatColor.WHITE + "Уровень 1: " + countCompletedByTier(completed, 1) + "/3",
                ChatColor.WHITE + "Уровень 2: " + countCompletedByTier(completed, 2) + "/4", 
                ChatColor.WHITE + "Уровень 3: " + countCompletedByTier(completed, 3) + "/3",
                ChatColor.WHITE + "Уровень 4: " + countCompletedByTier(completed, 4) + "/1",
                ChatColor.WHITE + "Уровень 5: " + countCompletedByTier(completed, 5) + "/1",
                "",
                ChatColor.YELLOW + "Общий прогресс: " + (completed.size() * 100 / 12) + "%"
            ));
        gui.setItem(40, progress);
        
        // Заполняем пустые слоты
        fillEmptySlots(gui);
        
        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "research_station");
    }
    
    private ItemStack createGuiItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }
    
    private boolean checkWaterCooling(org.bukkit.Location reactorLocation) {
        // Проверяем наличие воды вокруг реактора
        org.bukkit.Location base = reactorLocation.clone().subtract(3, 1, 3);
        
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                org.bukkit.Location waterLoc = base.clone().add(x, 0, z);
                if (waterLoc.getBlock().getType() == Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private int countCompletedByTier(java.util.Set<String> completed, int tier) {
        // Подсчитываем завершенные исследования по уровням - заглушка
        return 0;
    }
    
    public String getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }
    
    public void closeGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }
}
