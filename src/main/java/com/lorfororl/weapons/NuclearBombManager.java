package com.lorfororl.weapons;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NuclearBombManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, NuclearBomb> activeBombs;
    private final Map<UUID, Long> lastBombTime;
    
    public NuclearBombManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeBombs = new ConcurrentHashMap<>();
        this.lastBombTime = new HashMap<>();
    }
    
    public boolean deployBomb(Player player, Location location, int explosionPower, int radiationRadius) {
        // Проверяем разрешения
        if (!player.hasPermission("lorfororl.bomb.deploy")) {
            player.sendMessage("§c❌ У вас нет разрешения на размещение ядерных бомб!");
            return false;
        }
        
        // Проверяем кулдаун (10 минут между бомбами)
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastBombTime.getOrDefault(playerId, 0L);
        
        if (currentTime - lastTime < 600000) { // 10 минут
            long remainingTime = (600000 - (currentTime - lastTime)) / 1000;
            player.sendMessage(String.format("§c❌ Кулдаун! Осталось: %d секунд", remainingTime));
            return false;
        }
        
        // Проверяем исследование
        if (!plugin.getAdvancedResearchManager().isResearchCompleted(player, "nuclear_bomb")) {
            player.sendMessage("§c❌ Требуется исследование: Ядерная бомба!");
            return false;
        }
        
        // Проверяем лимит активных бомб
        long playerBombs = activeBombs.values().stream()
            .filter(bomb -> bomb.getOwner() != null && bomb.getOwner().equals(player))
            .count();
        
        if (playerBombs >= 1) {
            player.sendMessage("§c❌ У вас уже есть активная ядерная бомба!");
            return false;
        }
        
        // Проверяем безопасную зону (не ближе 100 блоков к спавну)
        Location spawnLocation = location.getWorld().getSpawnLocation();
        if (location.distance(spawnLocation) < 100) {
            player.sendMessage("§c❌ Нельзя размещать ядерные бомбы рядом со спавном!");
            return false;
        }
        
        // Создаем бомбу
        String bombId = "bomb_" + player.getName() + "_" + System.currentTimeMillis();
        NuclearBomb bomb = new NuclearBomb(bombId, location, player, explosionPower, radiationRadius, plugin);
        
        activeBombs.put(bombId, bomb);
        lastBombTime.put(playerId, currentTime);
        
        // Активируем бомбу
        bomb.arm();
        
        player.sendMessage("§4💣 Ядерная бомба размещена и активирована!");
        player.sendMessage("§e💡 ID бомбы: " + bombId);
        
        // Логирование
        plugin.getLogger().warning(String.format("Nuclear bomb deployed by %s at %s", 
            player.getName(), locationToString(location)));
        
        return true;
    }
    
    public boolean defuseBomb(Player player, String bombId) {
        NuclearBomb bomb = activeBombs.get(bombId);
        
        if (bomb == null) {
            player.sendMessage("§c❌ Бомба с ID '" + bombId + "' не найдена!");
            return false;
        }
        
        if (bomb.isExploded()) {
            player.sendMessage("§c❌ Эта бомба уже взорвалась!");
            activeBombs.remove(bombId);
            return false;
        }
        
        boolean success = bomb.defuse(player);
        
        if (success) {
            activeBombs.remove(bombId);
            plugin.getLogger().info(String.format("Nuclear bomb %s defused by %s", 
                bombId, player.getName()));
        }
        
        return success;
    }
    
    public ItemStack createNuclearBombItem() {
        ItemStack bomb = new ItemStack(Material.TNT);
        bomb.getItemMeta().setDisplayName("§4💣 Ядерная бомба");
        bomb.getItemMeta().setLore(java.util.Arrays.asList(
            "§7Оружие массового поражения",
            "§cВзрывная сила: 50 блоков",
            "§eРадиационная зона: 100 блоков",
            "§4⚠ КРАЙНЕ ОПАСНО!",
            "",
            "§6ПКМ - Разместить и активировать",
            "§7Время до взрыва: 15 секунд",
            "",
            "§cТребуется исследование: Ядерная бомба"
        ));
        bomb.getItemMeta().setCustomModelData(100010);
        
        return bomb;
    }
    
    public void listActiveBombs(Player player) {
        if (activeBombs.isEmpty()) {
            player.sendMessage("§a✅ Активных ядерных бомб нет");
            return;
        }
        
        player.sendMessage("§4💣 АКТИВНЫЕ ЯДЕРНЫЕ БОМБЫ:");
        player.sendMessage("§7" + "=".repeat(40));
        
        for (NuclearBomb bomb : activeBombs.values()) {
            String ownerName = bomb.getOwner() != null ? bomb.getOwner().getName() : "Неизвестно";
            String status = bomb.isArmed() ? "§cАКТИВНА" : "§7Неактивна";
            String timeLeft = bomb.isArmed() ? String.format("§e%d сек", bomb.getSecondsLeft()) : "§7-";
            
            player.sendMessage(String.format("§6ID: §f%s", bomb.getId()));
            player.sendMessage(String.format("§6Владелец: §f%s", ownerName));
            player.sendMessage(String.format("§6Статус: %s", status));
            player.sendMessage(String.format("§6Осталось: %s", timeLeft));
            player.sendMessage(String.format("§6Координаты: §f%s", locationToString(bomb.getLocation())));
            player.sendMessage("§7" + "-".repeat(30));
        }
    }
    
    public void emergencyDefuseAll(Player admin) {
        if (!admin.hasPermission("lorfororl.bomb.admin")) {
            admin.sendMessage("§c❌ Недостаточно прав!");
            return;
        }
        
        int defused = 0;
        for (NuclearBomb bomb : activeBombs.values()) {
            if (bomb.isArmed() && !bomb.isExploded()) {
                bomb.defuse(admin);
                defused++;
            }
        }
        
        activeBombs.clear();
        
        admin.sendMessage(String.format("§a✅ Экстренно обезврежено %d ядерных бомб!", defused));
        
        // Уведомляем всех игроков
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendTitle("§a§lВСЕ БОМБЫ ОБЕЗВРЕЖЕНЫ", 
                "§2Экстренное вмешательство администрации", 
                10, 60, 20);
        }
        
        plugin.getLogger().warning(String.format("Emergency defuse of all nuclear bombs by admin %s", 
            admin.getName()));
    }
    
    public void cleanupExpiredBombs() {
        activeBombs.entrySet().removeIf(entry -> entry.getValue().isExploded());
    }
    
    private String locationToString(Location loc) {
        return String.format("%.0f, %.0f, %.0f (%s)", 
            loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
    
    public Map<String, NuclearBomb> getActiveBombs() {
        return new HashMap<>(activeBombs);
    }
    
    public void shutdown() {
        // Экстренно обезвреживаем все активные бомбы при выключении сервера
        for (NuclearBomb bomb : activeBombs.values()) {
            if (bomb.isArmed() && !bomb.isExploded()) {
                // Принудительно останавливаем взрыв
                bomb.getLocation().getBlock().setType(Material.AIR);
            }
        }
        activeBombs.clear();
        plugin.getLogger().info("All nuclear bombs safely disarmed during shutdown");
    }
}
