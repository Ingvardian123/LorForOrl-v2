package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HologramManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, List<ArmorStand>> holograms;
    private BukkitRunnable updateTask;
    
    public HologramManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllHolograms();
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void createLaboratoryHologram(Location location) {
        List<String> lines = Arrays.asList(
            "§b§l◆═══════════════════════◆",
            "§3§l    🧪 §b§lЛАБОРАТОРИЯ §3§l🧪",
            "§b§l◆═══════════════════════◆",
            "§7┌─────────────────────────┐",
            "§aСтатус: §2●§a АКТИВНА",
            "§eИсследований: §6⚗ §e0§7/§e12",
            "§cЭнергия: §4⚡ §c0§7/§c1000 ед",
            "§dТемпература: §5🌡 §d23°C",
            "§bЭффективность: §3📊 §b100%",
            "§7└─────────────────────────┘",
            "§8▼ §7Взаимодействие §8▼",
            "§7» §eПКМ §7- Открыть интерфейс",
            "§7» §eShift+ПКМ §7- Диагностика"
        );
        createHologram("lab_" + locationToString(location), location.add(0, 3.5, 0), lines);
    }
    
    public void createReactorHologram(Location location) {
        List<String> lines = Arrays.asList(
            "§4§l☢═══════════════════════☢",
            "§c§l  ⚠ §4§lЯДЕРНЫЙ РЕАКТОР §c§l⚠",
            "§4§l☢═══════════════════════☢",
            "§7┌─────────────────────────┐",
            "§cСтатус: §4●§c НЕАКТИВЕН",
            "§6Температура: §e🌡 §620°C",
            "§9Топливо: §b⛽ §90§7/§964",
            "§aВыработка: §2⚡ §a0§7/§a1000 ед/тик",
            "§5Давление: §d📊 §51.0 атм",
            "§7└─────────────────────────┘",
            "§4§l⚠ РАДИАЦИОННАЯ ОПАСНОСТЬ ⚠",
            "§8▼ §7Управление §8▼",
            "§7» §eПКМ §7- Панель управления",
            "§7» §cShift+ПКМ §7- Аварийное отключение"
        );
        createHologram("reactor_" + locationToString(location), location.add(0, 4.5, 0), lines);
    }
    
    public void createSolarPanelHologram(Location location) {
        List<String> lines = Arrays.asList(
            "§e§l☀ СОЛНЕЧНАЯ ПАНЕЛЬ ☀",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "§aСтатус: §2●§a АКТИВНА",
            "§eВыработка: §625 ед/тик",
            "§bНакоплено: §30/1000 ед",
            "§dЭффективность: §5100%",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        );
        createHologram("solar_" + locationToString(location), location.add(0, 2, 0), lines);
    }
    
    public void createGeneratorHologram(Location location) {
        List<String> lines = Arrays.asList(
            "§b§l⚡ ЭНЕРГОГЕНЕРАТОР ⚡",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "§aСтатус: §2●§a ОЖИДАНИЕ",
            "§eГромоотводы: §65/8",
            "§9Накоплено: §b0/10000 ед",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "§7Ожидает грозы..."
        );
        createHologram("generator_" + locationToString(location), location.add(0, 3, 0), lines);
    }
    
    private void createHologram(String id, Location location, List<String> lines) {
        removeHologram(id);
        
        List<ArmorStand> stands = new ArrayList<>();
        double yOffset = 0;
        
        for (int i = lines.size() - 1; i >= 0; i--) {
            Location standLocation = location.clone().add(0, yOffset, 0);
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
            
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCanPickupItems(false);
            stand.setCustomName(lines.get(i));
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            stand.setSmall(true);
            
            stands.add(stand);
            yOffset += 0.25;
        }
        
        holograms.put(id, stands);
    }
    
    public void updateLaboratoryHologram(Location location, boolean active, int completedResearch, int energy) {
        String statusIcon = active ? "§2●" : "§4●";
        String statusText = active ? "§a АКТИВНА" : "§c НЕАКТИВНА";
        String energyBar = createAdvancedEnergyBar(energy, 1000);
        String researchIcon = completedResearch > 8 ? "§6⚗" : completedResearch > 4 ? "§e⚗" : "§7⚗";
        
        List<String> lines = Arrays.asList(
            "§b§l◆═══════════════════════◆",
            "§3§l    🧪 §b§lЛАБОРАТОРИЯ §3§l🧪",
            "§b§l◆═══════════════════════◆",
            "§7┌─────────────────────────┐",
            "§aСтатус: " + statusIcon + statusText,
            "§eИсследований: " + researchIcon + " §e" + completedResearch + "§7/§e12",
            "§cЭнергия: " + energyBar,
            "§dТемпература: §5🌡 §d" + (20 + (energy / 50)) + "°C",
            "§bЭффективность: §3📊 §b" + Math.min(100, energy / 10) + "%",
            "§7└─────────────────────────┘",
            active ? "§a§l✓ СИСТЕМЫ В НОРМЕ" : "§c§l⚠ ТРЕБУЕТСЯ ЭНЕРГИЯ",
            "§8▼ §7Взаимодействие §8▼",
            "§7» §eПКМ §7- Открыть интерфейс"
        );
        
        updateHologram("lab_" + locationToString(location), lines);
    }
    
    public void updateReactorHologram(Location location, boolean active, int temperature, int fuel, int output) {
        String status = active ? "§2●§a АКТИВЕН" : "§4●§c НЕАКТИВЕН";
        String tempColor = temperature > 800 ? "§4" : temperature > 600 ? "§6" : "§e";
        String warning = temperature > 900 ? "§4§l⚠ КРИТИЧЕСКИЙ ПЕРЕГРЕВ ⚠" : 
                        temperature > 800 ? "§c⚠ ПЕРЕГРЕВ ⚠" : "§c⚠ ОПАСНО ⚠";
        
        List<String> lines = Arrays.asList(
            "§4§l☢ ЯДЕРНЫЙ РЕАКТОР ☢",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "§cСтатус: " + status,
            "§6Температура: " + tempColor + temperature + "°C",
            "§9Топливо: §b" + fuel + "/64",
            "§aВыработка: §2" + output + "/1000 ед/тик",
            "§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            warning
        );
        
        updateHologram("reactor_" + locationToString(location), lines);
    }
    
    private void updateHologram(String id, List<String> newLines) {
        List<ArmorStand> stands = holograms.get(id);
        if (stands == null || stands.size() != newLines.size()) return;
        
        for (int i = 0; i < stands.size(); i++) {
            ArmorStand stand = stands.get(i);
            if (stand != null && !stand.isDead()) {
                stand.setCustomName(newLines.get(newLines.size() - 1 - i));
            }
        }
    }
    
    private String createEnergyBar(int current, int max) {
        int bars = 10;
        int filled = (int) ((double) current / max * bars);
        
        StringBuilder bar = new StringBuilder("§4⚡ ");
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append(" §f").append(current).append("/").append(max);
        
        return bar.toString();
    }

    private String createAdvancedEnergyBar(int current, int max) {
        int bars = 15;
        int filled = Math.min(bars, (int) ((double) current / max * bars));

        StringBuilder bar = new StringBuilder("§4⚡ ");

        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                if (i < bars * 0.3) {
                    bar.append("§c█"); // Красный для низкого уровня
                } else if (i < bars * 0.7) {
                    bar.append("§e█"); // Желтый для среднего
                } else {
                    bar.append("§a█"); // Зеленый для высокого
                }
            } else {
                bar.append("§8█"); // Серый для пустого
            }
        }

        // Добавляем процентный индикатор
        int percentage = (int) ((double) current / max * 100);
        String percentColor = percentage > 70 ? "§a" : percentage > 30 ? "§e" : "§c";

        bar.append(" ").append(percentColor).append(current).append("§7/§f").append(max);
        bar.append(" §8(").append(percentColor).append(percentage).append("%§8)");

        return bar.toString();
    }
    
    private void updateAllHolograms() {
        // Обновляем все голограммы с актуальными данными
        for (String id : holograms.keySet()) {
            if (id.startsWith("lab_")) {
                // Обновляем лабораторию
            } else if (id.startsWith("reactor_")) {
                // Обновляем реактор
            }
            // И так далее...
        }
    }
    
    public void removeHologram(String id) {
        List<ArmorStand> stands = holograms.remove(id);
        if (stands != null) {
            stands.forEach(ArmorStand::remove);
        }
    }
    
    private String locationToString(Location loc) {
        return loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }
    
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Удаляем все голограммы
        for (List<ArmorStand> stands : holograms.values()) {
            stands.forEach(ArmorStand::remove);
        }
        holograms.clear();
    }
}
