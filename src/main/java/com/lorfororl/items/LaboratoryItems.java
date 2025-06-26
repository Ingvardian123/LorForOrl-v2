package com.lorfororl.items;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class LaboratoryItems {
    
    private final LorForOrlPlugin plugin;
    private final NamespacedKey labItemKey;
    
    public LaboratoryItems(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.labItemKey = new NamespacedKey(plugin, "lab_item");
    }
    
    public ItemStack createLaboratoryCore() {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§bЯдро лаборатории");
        meta.setLore(Arrays.asList(
            "§7Центральный блок научной лаборатории",
            "§eИспользуется для создания лаборатории",
            "§cТолько для авторизованных исследователей!",
            "",
            "§8Размещается в центре структуры 3x3x3"
        ));
        meta.setCustomModelData(2001);
        
        meta.getPersistentDataContainer().set(labItemKey, PersistentDataType.STRING, "lab_core");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createResearchTerminal() {
        ItemStack item = new ItemStack(Material.LECTERN, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6Исследовательский терминал");
        meta.setLore(Arrays.asList(
            "§7Интерфейс для управления исследованиями",
            "§eПКМ - открыть меню исследований",
            "§7Показывает прогресс и доступные проекты",
            "",
            "§8Размещается рядом с лабораторией"
        ));
        meta.setCustomModelData(2002);
        
        meta.getPersistentDataContainer().set(labItemKey, PersistentDataType.STRING, "research_terminal");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createHazmatSuit() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§eКостюм химзащиты");
        meta.setLore(Arrays.asList(
            "§7Защищает от радиации и химических веществ",
            "§a+90% защита от радиации",
            "§a+50% защита от урона",
            "§cТребует исследование для крафта",
            "",
            "§8Результат исследования 'Костюм химзащиты'"
        ));
        meta.setCustomModelData(2003);
        
        meta.getPersistentDataContainer().set(labItemKey, PersistentDataType.STRING, "hazmat_suit");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createPowerArmor() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6Силовая броня");
        meta.setLore(Arrays.asList(
            "§7Мощная броня с встроенными системами",
            "§a+95% защита от радиации",
            "§a+80% защита от урона",
            "§a+50% скорость передвижения",
            "§cТребует исследование для крафта",
            "",
            "§8Результат исследования 'Силовая броня'"
        ));
        meta.setCustomModelData(2004);
        
        meta.getPersistentDataContainer().set(labItemKey, PersistentDataType.STRING, "power_armor");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createRailgun() {
        ItemStack item = new ItemStack(Material.CROSSBOW, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§cРельсотрон");
        meta.setLore(Arrays.asList(
            "§7Электромагнитное оружие дальнего действия",
            "§cОгромный урон на большой дистанции",
            "§eИспользует электрические заряды",
            "§cТребует исследование для крафта",
            "",
            "§8Результат исследования 'Рельсотрон'"
        ));
        meta.setCustomModelData(2005);
        
        meta.getPersistentDataContainer().set(labItemKey, PersistentDataType.STRING, "railgun");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isLaboratoryItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(labItemKey, PersistentDataType.STRING);
    }
    
    public String getLaboratoryItemType(ItemStack item) {
        if (!isLaboratoryItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(labItemKey, PersistentDataType.STRING);
    }
}
