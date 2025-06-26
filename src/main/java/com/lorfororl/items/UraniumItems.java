package com.lorfororl.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.lorfororl.LorForOrlPlugin;

import java.util.Arrays;

public class UraniumItems {
    
    private final LorForOrlPlugin plugin;
    private final NamespacedKey radiationKey;
    private final NamespacedKey uraniumTypeKey;
    
    public UraniumItems(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.radiationKey = new NamespacedKey(plugin, "radiation");
        this.uraniumTypeKey = new NamespacedKey(plugin, "uranium_type");
    }
    
    public ItemStack createUraniumDust(int amount) {
        ItemStack item = new ItemStack(Material.GUNPOWDER, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§aУрановая пыль");
        meta.setLore(Arrays.asList(
            "§7Радиоактивная пыль",
            "§c☢ Радиация: 0.01 у.е.",
            "§8Получается из центрифуги"
        ));
        meta.setCustomModelData(1001);
        
        // NBT данные
        meta.getPersistentDataContainer().set(radiationKey, PersistentDataType.DOUBLE, 0.01);
        meta.getPersistentDataContainer().set(uraniumTypeKey, PersistentDataType.STRING, "dust");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createUraniumIngot(int amount) {
        ItemStack item = new ItemStack(Material.IRON_INGOT, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§2Урановый слиток");
        meta.setLore(Arrays.asList(
            "§7Концентрированный уран",
            "§c☢ Радиация: 0.11 у.е.",
            "§8Крафтится из 9 пыли"
        ));
        meta.setCustomModelData(1002);
        
        // NBT данные
        meta.getPersistentDataContainer().set(radiationKey, PersistentDataType.DOUBLE, 0.11);
        meta.getPersistentDataContainer().set(uraniumTypeKey, PersistentDataType.STRING, "ingot");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack createUraniumBlock(int amount) {
        ItemStack item = new ItemStack(Material.IRON_BLOCK, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§4Урановый блок");
        meta.setLore(Arrays.asList(
            "§7Высокорадиоактивный блок",
            "§c☢ РАДИАЦИЯ: 1.0 у.е.",
            "§4⚠ ОПАСНО!"
        ));
        meta.setCustomModelData(1003);
        
        // NBT данные
        meta.getPersistentDataContainer().set(radiationKey, PersistentDataType.DOUBLE, 1.0);
        meta.getPersistentDataContainer().set(uraniumTypeKey, PersistentDataType.STRING, "block");
        
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isUraniumItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(uraniumTypeKey, PersistentDataType.STRING);
    }
    
    public double getRadiation(ItemStack item) {
        if (!isUraniumItem(item) && !isUraniumCapsule(item)) return 0.0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(radiationKey, PersistentDataType.DOUBLE, 0.0);
    }
    
    public String getUraniumType(ItemStack item) {
        if (!isUraniumItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(uraniumTypeKey, PersistentDataType.STRING);
    }

    public ItemStack createGeigerCounter(int amount) {
        ItemStack item = new ItemStack(Material.CLOCK, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6Дозиметр Гейгера");
        meta.setLore(Arrays.asList(
            "§7Измеряет уровень радиации",
            "§eПКМ - проверить свою радиацию",
            "§eShift+ПКМ - проверить область",
            "§8Крафтится из редстоуна и железа"
        ));
        meta.setCustomModelData(1004);
        
        // NBT данные
        meta.getPersistentDataContainer().set(uraniumTypeKey, PersistentDataType.STRING, "geiger");
        
        item.setItemMeta(meta);
        return item;
    }

    public boolean isGeigerCounter(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(uraniumTypeKey, PersistentDataType.STRING);
        return "geiger".equals(type);
    }

    public ItemStack createUraniumCapsule(int dustAmount) {
        ItemStack item = new ItemStack(Material.END_CRYSTAL, 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§bУрановая капсула");
        meta.setLore(Arrays.asList(
            "§7Контейнер для урановой пыли",
            "§eВместимость: §a" + dustAmount + "§7/§a500 пыли",
            "§c☢ Радиация: " + String.format("%.3f", dustAmount * 0.01) + " у.е.",
            "",
            "§7ПКМ с пылью - загрузить",
            "§7Shift+ПКМ - выгрузить пыль",
            "§8Можно размещать как блок"
        ));
        meta.setCustomModelData(1005);
        
        // NBT данные
        meta.getPersistentDataContainer().set(uraniumTypeKey, PersistentDataType.STRING, "capsule");
        meta.getPersistentDataContainer().set(radiationKey, PersistentDataType.DOUBLE, dustAmount * 0.01);
        
        // Сохраняем количество пыли в капсуле
        NamespacedKey dustAmountKey = new NamespacedKey(plugin, "dust_amount");
        meta.getPersistentDataContainer().set(dustAmountKey, PersistentDataType.INTEGER, dustAmount);
        
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEmptyCapsule() {
        return createUraniumCapsule(0);
    }

    public boolean isUraniumCapsule(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(uraniumTypeKey, PersistentDataType.STRING);
        return "capsule".equals(type);
    }

    public int getCapsuleDustAmount(ItemStack item) {
        if (!isUraniumCapsule(item)) return 0;
        NamespacedKey dustAmountKey = new NamespacedKey(plugin, "dust_amount");
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(dustAmountKey, PersistentDataType.INTEGER, 0);
    }

    public ItemStack updateCapsule(ItemStack capsule, int newDustAmount) {
        if (!isUraniumCapsule(capsule)) return capsule;
        
        ItemMeta meta = capsule.getItemMeta();
        NamespacedKey dustAmountKey = new NamespacedKey(plugin, "dust_amount");
        
        // Обновляем количество пыли
        meta.getPersistentDataContainer().set(dustAmountKey, PersistentDataType.INTEGER, newDustAmount);
        
        // Обновляем радиацию
        meta.getPersistentDataContainer().set(radiationKey, PersistentDataType.DOUBLE, newDustAmount * 0.01);
        
        // Обновляем лор
        meta.setLore(Arrays.asList(
            "§7Контейнер для урановой пыли",
            "§eВместимость: §a" + newDustAmount + "§7/§a500 пыли",
            "§c☢ Радиация: " + String.format("%.3f", newDustAmount * 0.01) + " у.е.",
            "",
            "§7ПКМ с пылью - загрузить",
            "§7Shift+ПКМ - выгрузить пыль",
            "§8Можно размещать как блок"
        ));
        
        capsule.setItemMeta(meta);
        return capsule;
    }
}
