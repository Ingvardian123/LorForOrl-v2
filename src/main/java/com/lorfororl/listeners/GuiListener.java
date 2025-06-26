package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public GuiListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String guiType = plugin.getGuiManager().getOpenGui(player);
        
        if (guiType == null) return;
        
        event.setCancelled(true); // Отменяем все клики в GUI
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }
        
        handleGuiClick(player, guiType, event.getSlot(), event.isRightClick());
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            plugin.getGuiManager().closeGui((Player) event.getPlayer());
        }
    }
    
    private void handleGuiClick(Player player, String guiType, int slot, boolean rightClick) {
        if (guiType.equals("laboratory")) {
            handleLaboratoryClick(player, slot, rightClick);
        } else if (guiType.startsWith("reactor:")) {
            handleReactorClick(player, slot, rightClick, guiType);
        } else if (guiType.startsWith("generator:")) {
            handleGeneratorClick(player, slot, rightClick, guiType);
        } else if (guiType.startsWith("solar:")) {
            handleSolarPanelClick(player, slot, rightClick, guiType);
        } else if (guiType.equals("research_station")) {
            handleResearchStationClick(player, slot, rightClick);
        }
    }
    
    private void handleLaboratoryClick(Player player, int slot, boolean rightClick) {
        switch (slot) {
            case 4: // Статус лаборатории
                player.sendMessage(ChatColor.GREEN + "Статус лаборатории обновлен!");
                plugin.getGuiManager().openLaboratoryGui(player);
                break;
                
            case 13: // Текущее исследование
                var activeResearch = plugin.getResearchManager().getActiveResearch(player);
                if (activeResearch != null && activeResearch.isComplete()) {
                    if (plugin.getResearchManager().completeResearch(player)) {
                        player.sendMessage(ChatColor.GREEN + "Исследование завершено!");
                        plugin.getGuiManager().openLaboratoryGui(player);
                    }
                } else if (activeResearch == null) {
                    // Открываем список исследований
                    player.closeInventory();
                    player.performCommand("lorfororl research list");
                }
                break;
                
            case 20: // Доступные исследования
                player.closeInventory();
                player.performCommand("lorfororl research list");
                break;
                
            case 24: // Завершенные исследования
                var completed = plugin.getResearchManager().getPlayerCompletedResearch(player);
                player.sendMessage(ChatColor.GREEN + "=== Завершенные исследования ===");
                for (String researchId : completed) {
                    var research = plugin.getResearchManager().getAvailableResearch().get(researchId);
                    if (research != null) {
                        player.sendMessage(ChatColor.WHITE + "• " + research.getName());
                    }
                }
                break;
                
            case 40: // Ускорение исследований
                int energy = plugin.getEnergyManager().getPlayerEnergy(player);
                if (energy >= 100) {
                    player.sendMessage(ChatColor.GREEN + "Исследования ускорены! (+25% скорость)");
                } else {
                    player.sendMessage(ChatColor.RED + "Недостаточно энергии для ускорения!");
                }
                break;
        }
    }
    
    private void handleReactorClick(Player player, int slot, boolean rightClick, String guiType) {
        String locationStr = guiType.substring("reactor:".length());
        // Здесь нужно парсить координаты из строки и получить реактор
        
        switch (slot) {
            case 4: // Статус реактора
                player.sendMessage(ChatColor.YELLOW + "Переключение состояния реактора...");
                // Логика включения/выключения реактора
                break;
                
            case 20: // Топливо
                if (rightClick) {
                    // Добавление топлива
                    if (player.getInventory().contains(Material.COAL, 1)) { // Заменить на урановое топливо
                        player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(Material.COAL, 1));
                        player.sendMessage(ChatColor.GREEN + "Топливо добавлено!");
                    } else {
                        player.sendMessage(ChatColor.RED + "У вас нет топлива!");
                    }
                }
                break;
                
            case 40: // Энергия
                int energy = 0; // Получить энергию реактора
                player.sendMessage(ChatColor.YELLOW + "Текущая выработка: " + energy + " ед/тик");
                break;
        }
    }
    
    private void handleGeneratorClick(Player player, int slot, boolean rightClick, String guiType) {
        switch (slot) {
            case 4: // Статус генератора
                player.sendMessage(ChatColor.YELLOW + "Генератор ожидает грозы...");
                break;
                
            case 24: // Накопители энергии
                if (rightClick) {
                    // Извлечение энергии
                    player.sendMessage(ChatColor.GREEN + "Энергия извлечена!");
                }
                break;
        }
    }
    
    private void handleSolarPanelClick(Player player, int slot, boolean rightClick, String guiType) {
        switch (slot) {
            case 4: // Статус панели
                plugin.getGuiManager().openSolarPanelGui(player, player.getLocation()); // Нужно получить правильную локацию
                break;
                
            case 13: // Накопленная энергия
                if (rightClick) {
                    player.sendMessage(ChatColor.GREEN + "Энергия извлечена из солнечной панели!");
                }
                break;
                
            case 22: // Обслуживание
                if (!rightClick) {
                    player.sendMessage(ChatColor.GREEN + "Солнечная панель очищена! Эффективность восстановлена.");
                }
                break;
        }
    }
    
    private void handleResearchStationClick(Player player, int slot, boolean rightClick) {
        if (slot >= 19 && slot <= 23) {
            // Клик по уровню исследований
            int tier = slot - 18;
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "=== Исследования уровня " + tier + " ===");
            
            var progression = plugin.getResearchManager().getProgression();
            var tierResearch = progression.getResearchByTier(tier);
            
            for (String researchId : tierResearch) {
                var research = plugin.getResearchManager().getAvailableResearch().get(researchId);
                if (research != null) {
                    boolean completed = plugin.getResearchManager().getPlayerCompletedResearch(player).contains(researchId);
                    boolean canStart = progression.canStartResearch(player, researchId);
                    
                    String status = completed ? ChatColor.GREEN + "[ЗАВЕРШЕНО]" : 
                                   canStart ? ChatColor.YELLOW + "[ДОСТУПНО]" : 
                                   ChatColor.RED + "[ЗАБЛОКИРОВАНО]";
                    
                    player.sendMessage(status + " " + research.getName() + " §7- " + research.getResearchTimeMinutes() + " мин");
                }
            }
        }
    }
}
