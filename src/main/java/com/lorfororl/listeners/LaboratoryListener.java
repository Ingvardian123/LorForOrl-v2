package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.laboratory.Laboratory;
import com.lorfororl.research.AdvancedResearchManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class LaboratoryListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    
    public LaboratoryListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();
        
        if (!isLaboratoryItem(item)) return;
        
        String itemType = getLaboratoryItemType(item);
        
        if ("lab_core".equals(itemType)) {
            if (!plugin.getLaboratoryManager().isAuthorized(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "У вас нет разрешения на создание лаборатории!");
                return;
            }
            
            Location location = event.getBlock().getLocation();
            
            // Проверяем через небольшую задержку, чтобы блок успел разместиться
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getLaboratoryManager().createLaboratory(player, location)) {
                    player.sendMessage(ChatColor.GREEN + "Лаборатория успешно создана!");
                    player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "Неправильная структура лаборатории!");
                    player.sendMessage(ChatColor.YELLOW + "Требуется структура 3x3x3:");
                    player.sendMessage(ChatColor.YELLOW + "Основание: железные блоки");
                    player.sendMessage(ChatColor.YELLOW + "Центр: алмазный блок");
                    player.sendMessage(ChatColor.YELLOW + "Углы 2 уровня: редстоун блоки");
                    player.sendMessage(ChatColor.YELLOW + "Верх: стеклянные блоки");
                }
            }, 1L);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        
        Player player = event.getPlayer();
        
        // Проверяем взаимодействие с исследовательским терминалом
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.LECTERN) {
            Laboratory lab = plugin.getLaboratoryManager().getLaboratoryAt(event.getClickedBlock().getLocation());
            
            if (lab != null) {
                if (!plugin.getLaboratoryManager().isAuthorized(player)) {
                    player.sendMessage(ChatColor.RED + "У вас нет доступа к этой лаборатории!");
                    event.setCancelled(true);
                    return;
                }
                
                plugin.getGuiManager().openLaboratoryGui(player);
                event.setCancelled(true);
            }
        }
        
        // Проверяем взаимодействие с ядром лаборатории
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DIAMOND_BLOCK) {
            Laboratory lab = plugin.getLaboratoryManager().getLaboratoryAt(event.getClickedBlock().getLocation());
            
            if (lab != null) {
                if (!plugin.getLaboratoryManager().isAuthorized(player)) {
                    player.sendMessage(ChatColor.RED + "У вас нет доступа к этой лаборатории!");
                    event.setCancelled(true);
                    return;
                }
                
                plugin.getGuiManager().openLaboratoryGui(player);
                event.setCancelled(true);
            }
        }
    }
    
    // Заглушки для методов LaboratoryItems
    private boolean isLaboratoryItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasCustomModelData();
    }
    
    private String getLaboratoryItemType(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return "";
        
        int modelData = item.getItemMeta().getCustomModelData();
        switch (modelData) {
            case 3001: return "lab_core";
            case 3002: return "research_terminal";
            default: return "";
        }
    }
    
    private void openResearchMenu(Player player, Laboratory lab) {
        AdvancedResearchManager researchManager = plugin.getAdvancedResearchManager();
        AdvancedResearchManager.ActiveAdvancedResearch activeResearch = researchManager.getActiveResearch(player);
        
        player.sendMessage("§b" + "=".repeat(50));
        player.sendMessage("§6⚗ §lИССЛЕДОВАТЕЛЬСКИЙ ТЕРМИНАЛ §6⚗");
        player.sendMessage("§b" + "=".repeat(50));
        
        if (activeResearch != null) {
            AdvancedResearchManager.AdvancedResearch research = researchManager.getAvailableResearch().get(activeResearch.getResearchId());
            double progress = activeResearch.getProgress() * 100;
            long remainingMinutes = activeResearch.getRemainingTime() / (1000 * 60);
            long remainingHours = remainingMinutes / 60;
            long remainingDays = remainingHours / 24;
        
            player.sendMessage("§e🔬 Текущее исследование: " + research.getName());
            player.sendMessage("§a📊 Прогресс: " + String.format("%.2f%%", progress));
        
            if (remainingDays > 0) {
                player.sendMessage("§7⏰ Осталось: " + remainingDays + " дн. " + (remainingHours % 24) + " ч. " + (remainingMinutes % 60) + " мин.");
            } else if (remainingHours > 0) {
                player.sendMessage("§7⏰ Осталось: " + remainingHours + " ч. " + (remainingMinutes % 60) + " мин.");
            } else {
                player.sendMessage("§7⏰ Осталось: " + remainingMinutes + " мин.");
            }
        
            // Прогресс-бар
            int barLength = 30;
            int filled = (int) (progress / 100 * barLength);
            StringBuilder bar = new StringBuilder("§a");
            for (int i = 0; i < barLength; i++) {
                if (i < filled) {
                    bar.append("█");
                } else if (i == filled) {
                    bar.append("§e█§7");
                } else {
                    bar.append("█");
                }
            }
            player.sendMessage("§7[" + bar.toString() + "§7]");
        
            if (activeResearch.isComplete()) {
                player.sendMessage("");
                player.sendMessage("§a✅ §lИССЛЕДОВАНИЕ ЗАВЕРШЕНО!");
                player.sendMessage("§eНажмите еще раз для получения результата.");
            }
        } else {
            player.sendMessage("§7📚 Доступные исследования:");
            player.sendMessage("");
        
            for (AdvancedResearchManager.AdvancedResearch research : researchManager.getAvailableResearch().values()) {
                if (!researchManager.isResearchCompleted(player, research.getId())) {
                    player.sendMessage(research.getCategory().getDisplayName() + " " + research.getName());
                    player.sendMessage("§7└─ " + research.getDescription());
                    player.sendMessage("§7└─ ⏰ Время: " + formatResearchTime(research.getResearchTimeMinutes()));
                    player.sendMessage("§7└─ 📋 Команда: §e/lorfororl research start " + research.getId());
                    player.sendMessage("");
                }
            }
        
            Set<String> completed = researchManager.getCompletedResearch(player);
            if (!completed.isEmpty()) {
                player.sendMessage("§a✅ Завершенные исследования: " + completed.size());
            }
        }
        
        player.sendMessage("§b" + "=".repeat(50));
    }

    private String formatResearchTime(long minutes) {
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d дн. %d ч.", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%d ч.", hours);
        } else {
            return String.format("%d мин.", minutes);
        }
    }
    
    private void showLaboratoryStatus(Player player, Laboratory lab) {
        player.sendMessage(ChatColor.GREEN + "=== Статус лаборатории ===");
        player.sendMessage(ChatColor.WHITE + "Состояние: " + (lab.isActive() ? ChatColor.GREEN + "АКТИВНА" : ChatColor.RED + "НЕАКТИВНА"));
        
        if (lab.getCurrentResearch() != null) {
            player.sendMessage(ChatColor.YELLOW + "Текущее исследование: " + lab.getCurrentResearch());
        } else {
            player.sendMessage(ChatColor.GRAY + "Исследования не проводятся");
        }
        
        player.sendMessage(ChatColor.GRAY + "Используйте исследовательский терминал для управления");
    }
    
    private void giveResearchResult(Player player, String researchId) {
        ItemStack result = null;
        
        switch (researchId) {
            case "hazmat_suit":
                result = createHazmatSuit();
                break;
            case "power_armor":
                result = createPowerArmor();
                break;
            case "railgun":
                result = createRailgun();
                break;
            case "nuclear_reactor":
                // Выдаем компоненты для реактора
                player.getInventory().addItem(new ItemStack(Material.BEACON, 1)); // Ядро реактора
                player.sendMessage(ChatColor.GREEN + "Получены чертежи ядерного реактора!");
                return;
            case "electric_vehicle":
                player.getInventory().addItem(new ItemStack(Material.MINECART, 1)); // Электрокар
                player.sendMessage(ChatColor.GREEN + "Получен электротранспорт!");
                return;
            case "auto_miner":
                player.getInventory().addItem(new ItemStack(Material.DISPENSER, 1)); // Автошахтер
                player.sendMessage(ChatColor.GREEN + "Получен автоматический шахтер!");
                return;
        }
        
        if (result != null) {
            player.getInventory().addItem(result);
            player.sendMessage(ChatColor.GREEN + "Получен результат исследования!");
        }
    }
    
    // Заглушки для создания предметов
    private ItemStack createHazmatSuit() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        item.getItemMeta().setDisplayName("§eКостюм химзащиты");
        item.getItemMeta().setCustomModelData(2003);
        item.setItemMeta(item.getItemMeta());
        return item;
    }
    
    private ItemStack createPowerArmor() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        item.getItemMeta().setDisplayName("§bСиловая броня");
        item.getItemMeta().setCustomModelData(2004);
        item.setItemMeta(item.getItemMeta());
        return item;
    }
    
    private ItemStack createRailgun() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        item.getItemMeta().setDisplayName("§cРельсотрон");
        item.getItemMeta().setCustomModelData(2005);
        item.setItemMeta(item.getItemMeta());
        return item;
    }
}
