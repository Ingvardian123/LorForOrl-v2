package com.lorfororl.items;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AdvancedItems {
    
    private final LorForOrlPlugin plugin;
    
    public AdvancedItems(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    // Костюм химзащиты
    public ItemStack createHazmatSuit() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.YELLOW + "Костюм химзащиты");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Защищает от радиации",
            ChatColor.GREEN + "Защита от радиации: 90%",
            ChatColor.BLUE + "Прочность: 500/500",
            "",
            ChatColor.GOLD + "Исследование: Костюм химзащиты"
        ));
        
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(100001);
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Электротранспорт
    public ItemStack createElectricVehicle() {
        ItemStack item = new ItemStack(Material.MINECART);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.AQUA + "Электрокар");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Быстрый электрический транспорт",
            ChatColor.GREEN + "Скорость: 200% от обычной",
            ChatColor.BLUE + "Энергия: 1000/1000",
            ChatColor.YELLOW + "Расход: 1 ед/блок",
            "",
            ChatColor.GOLD + "Исследование: Электротранспорт"
        ));
        
        meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(100002);
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Автоматический шахтер
    public ItemStack createAutoMiner() {
        ItemStack item = new ItemStack(Material.DISPENSER);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.DARK_GRAY + "Автоматический шахтер");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Автоматически добывает ресурсы",
            ChatColor.GREEN + "Радиус: 5x5x5 блоков",
            ChatColor.BLUE + "Энергия: 500/500",
            ChatColor.YELLOW + "Расход: 10 ед/блок",
            ChatColor.RED + "Скорость: 1 блок/5 сек",
            "",
            ChatColor.GOLD + "Исследование: Автоматический шахтер"
        ));
        
        meta.addEnchant(Enchantment.DIG_SPEED, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(100003);
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Квантовый процессор
    public ItemStack createQuantumProcessor() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Квантовый процессор");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Сверхмощный вычислительный блок",
            ChatColor.GREEN + "Ускорение исследований: +200%",
            ChatColor.BLUE + "Энергия: 2000/2000",
            ChatColor.YELLOW + "Расход: 50 ед/тик",
            "",
            ChatColor.GOLD + "Исследование: Квантовые технологии"
        ));
        
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(100004);
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Термоядерное топливо
    public ItemStack createFusionFuel() {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.DARK_AQUA + "Термоядерное топливо"); // Заменяем CYAN на DARK_AQUA
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Топливо для термоядерного реактора",
            ChatColor.GREEN + "Энергоемкость: 10000 ед",
            ChatColor.BLUE + "Время горения: 1 час",
            ChatColor.RED + "Радиоактивность: Высокая",
            "",
            ChatColor.GOLD + "Исследование: Термоядерный реактор"
        ));
        
        meta.addEnchant(Enchantment.FIRE_ASPECT, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setCustomModelData(100005);
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Солнечная панель (набор)
    public ItemStack createSolarPanelKit() {
        ItemStack item = new ItemStack(Material.DAYLIGHT_DETECTOR);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.YELLOW + "Набор солнечной панели");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Для создания солнечной панели 3x3",
            ChatColor.GREEN + "Выработка: 25 ед/тик (днем)",
            ChatColor.BLUE + "Накопление: 1000 ед",
            ChatColor.YELLOW + "Требует прямой солнечный свет",
            "",
            ChatColor.RED + "Дополнительно требуется:",
            ChatColor.WHITE + "• 9x Датчик дневного света",
            ChatColor.WHITE + "• 9x Медный блок",
            "",
            ChatColor.GOLD + "ПКМ - Разместить основание"
        ));
        
        meta.setCustomModelData(100006);
        item.setItemMeta(meta);
        return item;
    }
}
