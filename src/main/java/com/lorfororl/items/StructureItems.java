package com.lorfororl.items;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.structures.StructureBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class StructureItems {
    
    private final LorForOrlPlugin plugin;
    private final NamespacedKey structureKey;
    
    public StructureItems(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.structureKey = new NamespacedKey(plugin, "structure_type");
    }
    
    public ItemStack createLaboratoryKit() {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§bНабор для лаборатории");
        meta.setLore(Arrays.asList(
            "§7Портативный набор для строительства лаборатории",
            "§eПКМ по земле - начать строительство",
            "§cТребует дополнительные ресурсы:",
            "§7• 9x Железный блок",
            "§7• 4x Редстоун блок", 
            "§7• 9x Стекло",
            "",
            "§8Размер: 3x3x3"
        ));
        meta.setCustomModelData(3001);
        
        meta.getPersistentDataContainer().set(structureKey, PersistentDataType.STRING, "LABORATORY");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createReactorKit() {
        ItemStack item = new ItemStack(Material.BEACON, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§4Набор для ядерного реактора");
        meta.setLore(Arrays.asList(
            "§7Портативный набор для строительства реактора",
            "§eПКМ по земле - начать строительство",
            "§cТребует дополнительные ресурсы:",
            "§7• 80x Обсидиан",
            "§7• 64x Железный блок",
            "§7• 32x Медный блок", // Обновили с свинца на медь
            "§7• 8x Алмазный блок",
            "§7• 24x Редстоун блок",
            "§7• 16x Золотой блок",
            "",
            "§8Размер: 5x5x5",
            "§c⚠ ОПАСНО! Может взорваться!"
        ));
        meta.setCustomModelData(3002);
        
        meta.getPersistentDataContainer().set(structureKey, PersistentDataType.STRING, "NUCLEAR_REACTOR");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createGeneratorKit() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6Набор для энергогенератора");
        meta.setLore(Arrays.asList(
            "§7Портативный набор для строительства генератора",
            "§eПКМ по земле - начать строительство",
            "§cТребует дополнительные ресурсы:",
            "§7• 8x Медный блок",
            "§7• 5x Громоотвод",
            "§7• 2x Золотой блок",
            "§7• 4x Редстоун блок",
            "",
            "§8Размер: 3x3x3",
            "§aВырабатывает энергию от молний"
        ));
        meta.setCustomModelData(3003);
        
        meta.getPersistentDataContainer().set(structureKey, PersistentDataType.STRING, "ENERGY_GENERATOR");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createResearchStationKit() {
        ItemStack item = new ItemStack(Material.ENCHANTING_TABLE, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§5Набор для исследовательской станции");
        meta.setLore(Arrays.asList(
            "§7Портативный набор для строительства станции",
            "§eПКМ по земле - начать строительство",
            "§cТребует дополнительные ресурсы:",
            "§7• 8x Кварцевый блок",
            "§7• 4x Книжная полка",
            "§7• 4x Лекторн",
            "§7• 8x Алмаз",
            "",
            "§8Размер: 3x3x2",
            "§aУскоряет исследования на 50%"
        ));
        meta.setCustomModelData(3004);
        
        meta.getPersistentDataContainer().set(structureKey, PersistentDataType.STRING, "RESEARCH_STATION");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createConstructionTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§eИнструмент строителя");
        meta.setLore(Arrays.asList(
            "§7Специальный инструмент для строительства",
            "§eПКМ по ядру - добавить ресурсы",
            "§eShift+ПКМ - проверить прогресс",
            "§cShift+ЛКМ - отменить строительство",
            "",
            "§8Необходим для работы со структурами"
        ));
        meta.setCustomModelData(3005);
        
        meta.getPersistentDataContainer().set(structureKey, PersistentDataType.STRING, "CONSTRUCTION_TOOL");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isStructureItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(structureKey, PersistentDataType.STRING);
    }
    
    public String getStructureType(ItemStack item) {
        if (!isStructureItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(structureKey, PersistentDataType.STRING);
    }
    
    public StructureBuilder.StructureType getStructureTypeEnum(ItemStack item) {
        String typeString = getStructureType(item);
        if (typeString == null) return null;
        
        try {
            return StructureBuilder.StructureType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
