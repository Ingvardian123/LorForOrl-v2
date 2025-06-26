package com.lorfororl.structures;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StructureBuilder {

    public enum StructureType {
        LABORATORY("laboratory", "Лаборатория"),
        NUCLEAR_REACTOR("nuclear_reactor", "Ядерный реактор"),
        ENERGY_GENERATOR("energy_generator", "Энергогенератор"),
        RESEARCH_STATION("research_station", "Исследовательская станция"),
        SOLAR_PANEL("solar_panel", "Солнечная панель");

        private final String id;
        private final String displayName;

        StructureType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }

        public static StructureType fromId(String id) {
            for (StructureType type : values()) {
                if (type.getId().equals(id)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    private final LorForOrlPlugin plugin;
    private final Map<String, StructureTemplate> templates;
    private final Map<UUID, BuildingProcess> activeBuildingProcesses;
    
    public StructureBuilder(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.templates = new HashMap<>();
        this.activeBuildingProcesses = new HashMap<>();
        initializeTemplates();
    }
    
    private void initializeTemplates() {
        // Шаблон лаборатории 7x7x5
        StructureTemplate laboratory = new StructureTemplate("laboratory", "Лаборатория");
        laboratory.addLayer(0, new Material[][] {
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.BEACON, Material.DIAMOND_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
        });
        
        // Добавляем остальные слои лаборатории
        for (int y = 1; y < 4; y++) {
            laboratory.addLayer(y, new Material[][] {
                {Material.IRON_BLOCK, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.IRON_BLOCK},
                {Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.GLASS},
                {Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.GLASS},
                {Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.GLASS},
                {Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.GLASS},
                {Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.GLASS},
                {Material.IRON_BLOCK, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.IRON_BLOCK}
            });
        }
        
        // Крыша лаборатории
        laboratory.addLayer(4, new Material[][] {
            {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.EMERALD_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK}
        });
        
        templates.put("laboratory", laboratory);
        
        // Шаблон реактора 5x5x5
        StructureTemplate reactor = new StructureTemplate("reactor", "Ядерный реактор");
        
        // Основание реактора
        reactor.addLayer(0, new Material[][] {
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
        });
        
        // Средние слои реактора
        for (int y = 1; y < 4; y++) {
            reactor.addLayer(y, new Material[][] {
                {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
                {Material.IRON_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.IRON_BLOCK},
                {Material.IRON_BLOCK, Material.AIR, y == 2 ? Material.BEACON : Material.AIR, Material.AIR, Material.IRON_BLOCK},
                {Material.IRON_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.IRON_BLOCK},
                {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK}
            });
        }
        
        // Крыша реактора
        reactor.addLayer(4, new Material[][] {
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
        });
        
        templates.put("reactor", reactor);
        
        // Шаблон солнечной панели 3x3x2
        StructureTemplate solarPanel = new StructureTemplate("solar_panel", "Солнечная панель");
        
        // Основание солнечной панели
        solarPanel.addLayer(0, new Material[][] {
            {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.REDSTONE_BLOCK, Material.IRON_BLOCK},
            {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK}
        });
        
        // Панели
        solarPanel.addLayer(1, new Material[][] {
            {Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS},
            {Material.BLUE_STAINED_GLASS, Material.DAYLIGHT_DETECTOR, Material.BLUE_STAINED_GLASS},
            {Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS}
        });
        
        templates.put("solar_panel", solarPanel);
    }
    
    public boolean startBuilding(Player player, String templateName, Location startLocation) {
        if (activeBuildingProcesses.containsKey(player.getUniqueId())) {
            player.sendMessage("§cУ вас уже идет строительство!");
            return false;
        }
        
        StructureTemplate template = templates.get(templateName);
        if (template == null) {
            player.sendMessage("§cШаблон структуры не найден!");
            return false;
        }
        
        // Проверяем наличие ресурсов
        Map<Material, Integer> requiredResources = calculateRequiredResources(template);
        if (!hasRequiredResources(player, requiredResources)) {
            player.sendMessage("§cНедостаточно ресурсов для строительства!");
            showRequiredResources(player, requiredResources);
            return false;
        }
        
        // Проверяем свободное место
        if (!isAreaClear(startLocation, template)) {
            player.sendMessage("§cОбласть для строительства занята!");
            return false;
        }
        
        // Забираем ресурсы
        if (!consumeResources(player, requiredResources)) {
            player.sendMessage("§cОшибка при изъятии ресурсов!");
            return false;
        }
        
        // Начинаем строительство
        BuildingProcess process = new BuildingProcess(player, template, startLocation);
        activeBuildingProcesses.put(player.getUniqueId(), process);
        
        player.sendMessage("§aНачато строительство: " + template.getName());
        player.sendMessage("§eВремя строительства: " + formatTime(template.getBuildTime()));
        
        startBuildingAnimation(process);
        
        return true;
    }
    
    private void startBuildingAnimation(BuildingProcess process) {
        new BukkitRunnable() {
            private int currentLayer = 0;
            private int currentBlock = 0;
            private long lastBlockTime = System.currentTimeMillis();
            
            @Override
            public void run() {
                if (!process.getPlayer().isOnline()) {
                    cancel();
                    activeBuildingProcesses.remove(process.getPlayer().getUniqueId());
                    return;
                }
                
                StructureTemplate template = process.getTemplate();
                
                if (currentLayer >= template.getLayers().size()) {
                    // Строительство завершено
                    completeBuilding(process);
                    cancel();
                    return;
                }
                
                // Получаем текущий слой
                Material[][] layer = template.getLayers().get(currentLayer);
                
                // Размещаем блоки с задержкой
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBlockTime >= 100) { // 100мс между блоками
                    placeNextBlock(process, layer, currentLayer);
                    lastBlockTime = currentTime;
                    currentBlock++;
                    
                    // Переходим к следующему слою
                    if (currentBlock >= layer.length * layer[0].length) {
                        currentLayer++;
                        currentBlock = 0;
                        
                        // Эффекты завершения слоя
                        showLayerCompleteEffects(process.getStartLocation().clone().add(0, currentLayer, 0));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void placeNextBlock(BuildingProcess process, Material[][] layer, int layerY) {
        int blocksPerLayer = layer.length * layer[0].length;
        int currentBlockIndex = 0;
        
        // Находим текущий блок для размещения
        for (int x = 0; x < layer.length; x++) {
            for (int z = 0; z < layer[x].length; z++) {
                if (currentBlockIndex == process.getCurrentBlock()) {
                    Location blockLoc = process.getStartLocation().clone().add(x, layerY, z);
                    Material material = layer[x][z];
                    
                    if (material != Material.AIR) {
                        blockLoc.getBlock().setType(material);
                        
                        // Эффекты размещения блока
                        showBlockPlaceEffects(blockLoc, material);
                    }
                    
                    process.incrementCurrentBlock();
                    return;
                }
                currentBlockIndex++;
            }
        }
    }
    
    private void showBlockPlaceEffects(Location location, Material material) {
        // Частицы размещения
        location.getWorld().spawnParticle(Particle.BLOCK, location.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1, material.createBlockData());
        
        // Звук размещения
        location.getWorld().playSound(location, Sound.BLOCK_STONE_PLACE, 0.5f, 1.0f);
    }
    
    private void showLayerCompleteEffects(Location location) {
        // Кольцо частиц при завершении слоя
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double x = location.getX() + 3 * Math.cos(angle);
            double z = location.getZ() + 3 * Math.sin(angle);
            
            Location particleLoc = new Location(location.getWorld(), x, location.getY(), z);
            location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
        }
        
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }
    
    private void completeBuilding(BuildingProcess process) {
        Player player = process.getPlayer();
        String templateName = process.getTemplate().getId();
        Location location = process.getStartLocation();
        
        // Убираем из активных процессов
        activeBuildingProcesses.remove(player.getUniqueId());
        
        // Уведомляем игрока
        player.sendMessage("§a§l✓ СТРОИТЕЛЬСТВО ЗАВЕРШЕНО!");
        player.sendMessage("§eСтруктура '" + process.getTemplate().getName() + "' построена!");
        
        // Эффекты завершения
        showCompletionEffects(location, process.getTemplate());
        
        // Регистрируем структуру в системе
        registerStructure(player, templateName, location);
        
        // Достижение
        plugin.getAchievementManager().unlockAchievement(player, "builder_" + templateName);
    }
    
    private void showCompletionEffects(Location center, StructureTemplate template) {
        // Фейерверк
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 5;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + template.getLayers().size() + Math.random() * 5;
            
            Location fireworkLoc = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(Particle.FIREWORK, fireworkLoc, 1, 0, 0, 0, 0.1);
        }
        
        center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
    }
    
    private void registerStructure(Player player, String type, Location location) {
        // Регистрируем структуру в соответствующем менеджере
        switch (type) {
            case "laboratory":
                plugin.getLaboratoryManager().registerLaboratory(location, player.getUniqueId());
                break;
            case "reactor":
                plugin.getReactorManager().registerReactor(location, player.getUniqueId());
                break;
            case "solar_panel":
                plugin.getSolarPanelManager().registerSolarPanel(location, player.getUniqueId());
                break;
        }
    }
    
    private Map<Material, Integer> calculateRequiredResources(StructureTemplate template) {
        Map<Material, Integer> resources = new HashMap<>();
        
        for (Material[][] layer : template.getLayers()) {
            for (Material[] row : layer) {
                for (Material material : row) {
                    if (material != Material.AIR) {
                        resources.put(material, resources.getOrDefault(material, 0) + 1);
                    }
                }
            }
        }
        
        return resources;
    }
    
    private boolean hasRequiredResources(Player player, Map<Material, Integer> required) {
        for (Map.Entry<Material, Integer> entry : required.entrySet()) {
            if (countPlayerItems(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    private int countPlayerItems(Player player, Material material) {
        int count = 0;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private boolean consumeResources(Player player, Map<Material, Integer> required) {
        // Сначала проверяем, что у игрока достаточно ресурсов
        if (!hasRequiredResources(player, required)) {
            return false;
        }
        
        // Забираем ресурсы
        for (Map.Entry<Material, Integer> entry : required.entrySet()) {
            removePlayerItems(player, entry.getKey(), entry.getValue());
        }
        
        return true;
    }
    
    private void removePlayerItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                int toRemove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;
                
                if (item.getAmount() <= 0) {
                    player.getInventory().remove(item);
                }
            }
        }
    }
    
    private void showRequiredResources(Player player, Map<Material, Integer> required) {
        player.sendMessage("§c📋 Требуемые ресурсы:");
        
        for (Map.Entry<Material, Integer> entry : required.entrySet()) {
            int has = countPlayerItems(player, entry.getKey());
            int needed = entry.getValue();
            String status = has >= needed ? "§a✅" : "§c❌";
            
            player.sendMessage(String.format("%s §7%s: §f%d§7/§e%d", 
                status, getItemDisplayName(entry.getKey()), has, needed));
        }
    }
    
    private String getItemDisplayName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }
    
    private boolean isAreaClear(Location start, StructureTemplate template) {
        for (int y = 0; y < template.getLayers().size(); y++) {
            Material[][] layer = template.getLayers().get(y);
            for (int x = 0; x < layer.length; x++) {
                for (int z = 0; z < layer[x].length; z++) {
                    Location checkLoc = start.clone().add(x, y, z);
                    if (layer[x][z] != Material.AIR && checkLoc.getBlock().getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
    
    public boolean cancelBuilding(Player player) {
        BuildingProcess process = activeBuildingProcesses.remove(player.getUniqueId());
        if (process == null) {
            player.sendMessage("§cУ вас нет активного строительства!");
            return false;
        }
        
        player.sendMessage("§eСтроительство отменено!");
        // Можно добавить возврат части ресурсов
        
        return true;
    }
    
    public BuildingProcess getBuildingProcess(Player player) {
        return activeBuildingProcesses.get(player.getUniqueId());
    }
    
    public Set<String> getAvailableTemplates() {
        return templates.keySet();
    }
    
    public StructureTemplate getTemplate(String name) {
        return templates.get(name);
    }
    
    // Классы для структур
    public static class StructureTemplate {
        private final String id;
        private final String name;
        private final List<Material[][]> layers;
        private final long buildTime; // в секундах
        
        public StructureTemplate(String id, String name) {
            this.id = id;
            this.name = name;
            this.layers = new ArrayList<>();
            this.buildTime = 60; // По умолчанию 1 минута
        }
        
        public void addLayer(int y, Material[][] layer) {
            // Расширяем список если нужно
            while (layers.size() <= y) {
                layers.add(null);
            }
            layers.set(y, layer);
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public List<Material[][]> getLayers() { return layers; }
        public long getBuildTime() { return buildTime; }
    }
    
    public static class BuildingProcess {
        private final Player player;
        private final StructureTemplate template;
        private final Location startLocation;
        private final long startTime;
        private int currentBlock;
        
        public BuildingProcess(Player player, StructureTemplate template, Location startLocation) {
            this.player = player;
            this.template = template;
            this.startLocation = startLocation;
            this.startTime = System.currentTimeMillis();
            this.currentBlock = 0;
        }
        
        public Player getPlayer() { return player; }
        public StructureTemplate getTemplate() { return template; }
        public Location getStartLocation() { return startLocation; }
        public long getStartTime() { return startTime; }
        public int getCurrentBlock() { return currentBlock; }
        
        public void incrementCurrentBlock() { currentBlock++; }
        
        public double getProgress() {
            int totalBlocks = 0;
            for (Material[][] layer : template.getLayers()) {
                totalBlocks += layer.length * layer[0].length;
            }
            return totalBlocks > 0 ? (double) currentBlock / totalBlocks : 0.0;
        }
    }
}
