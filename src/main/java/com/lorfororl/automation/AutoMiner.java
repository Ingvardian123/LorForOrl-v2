package com.lorfororl.automation;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.energy.EnergyConsumer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoMiner implements EnergyConsumer {
    
    private final LorForOrlPlugin plugin;
    private final Location location;
    private final UUID ownerId;
    private final Set<Material> allowedBlocks;
    private final Set<Material> blacklistedBlocks;
    
    // Настройки
    private int radius = 5;
    private int depth = 5;
    private int energyCapacity = 10000;
    private int currentEnergy = 0;
    private int energyPerBlock = 50;
    private boolean isActive = false;
    private MiningMode mode = MiningMode.SELECTIVE;
    
    // Рабочие переменные
    private Queue<Location> miningQueue;
    private BukkitRunnable miningTask;
    private long lastMiningTime = 0;
    private int blocksMinedToday = 0;
    private int totalBlocksMined = 0;
    
    // Статистика
    private Map<Material, Integer> minedResources;
    private long operationTime = 0;
    
    public AutoMiner(LorForOrlPlugin plugin, Location location, UUID ownerId) {
        this.plugin = plugin;
        this.location = location.clone();
        this.ownerId = ownerId;
        this.allowedBlocks = new HashSet<>();
        this.blacklistedBlocks = new HashSet<>();
        this.miningQueue = new LinkedList<>();
        this.minedResources = new HashMap<>();
        
        initializeDefaultSettings();
        generateMiningQueue();
    }
    
    private void initializeDefaultSettings() {
        // Разрешенные блоки по умолчанию
        allowedBlocks.addAll(Arrays.asList(
            Material.STONE, Material.DEEPSLATE,
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE
        ));
        
        // Запрещенные блоки
        blacklistedBlocks.addAll(Arrays.asList(
            Material.BEDROCK, Material.BARRIER,
            Material.SPAWNER, Material.END_PORTAL_FRAME,
            Material.WATER, Material.LAVA
        ));
    }
    
    public void start() {
        if (isActive) return;
        
        isActive = true;
        operationTime = System.currentTimeMillis();
        
        miningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || currentEnergy < energyPerBlock) {
                    return;
                }
                
                mineNextBlock();
            }
        };
        
        miningTask.runTaskTimer(plugin, 0L, 100L); // Каждые 5 секунд
        
        // Визуальные эффекты запуска
        showStartupEffects();
        
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§a⚡ Автошахтер запущен!");
            owner.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        }
    }
    
    public void stop() {
        if (!isActive) return;
        
        isActive = false;
        
        if (miningTask != null) {
            miningTask.cancel();
            miningTask = null;
        }
        
        // Визуальные эффекты остановки
        showShutdownEffects();
        
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§c⚡ Автошахтер остановлен!");
            owner.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
        }
    }
    
    private void mineNextBlock() {
        if (miningQueue.isEmpty()) {
            generateMiningQueue();
            if (miningQueue.isEmpty()) {
                stop();
                return;
            }
        }
        
        Location targetLoc = miningQueue.poll();
        Block block = targetLoc.getBlock();
        
        if (!canMineBlock(block)) {
            return;
        }
        
        // Потребляем энергию
        currentEnergy -= energyPerBlock;
        
        // Добываем блок
        Material blockType = block.getType();
        Collection<ItemStack> drops = block.getDrops();
        
        // Статистика
        minedResources.put(blockType, minedResources.getOrDefault(blockType, 0) + 1);
        blocksMinedToday++;
        totalBlocksMined++;
        lastMiningTime = System.currentTimeMillis();
        
        // Визуальные эффекты добычи
        showMiningEffects(targetLoc, blockType);
        
        // Ломаем блок
        block.setType(Material.AIR);
        
        // Собираем дропы
        collectDrops(targetLoc, drops);
        
        // Уведомление владельца
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline() && totalBlocksMined % 50 == 0) {
            owner.sendMessage(String.format("§6⛏ Автошахтер: добыто %d блоков", totalBlocksMined));
        }
    }
    
    private boolean canMineBlock(Block block) {
        Material type = block.getType();
        
        if (blacklistedBlocks.contains(type)) {
            return false;
        }
        
        switch (mode) {
            case SELECTIVE:
                return allowedBlocks.contains(type);
            case ORES_ONLY:
                return type.name().contains("_ORE");
            case ALL_BLOCKS:
                return type.isSolid() && !type.isAir();
            default:
                return false;
        }
    }
    
    private void generateMiningQueue() {
        miningQueue.clear();
        
        Location center = location.clone().subtract(0, 1, 0); // Начинаем под автошахтером
        
        // Генерируем спиральную очередь добычи
        for (int y = 0; y < depth; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) { // Круглая область
                        Location loc = center.clone().add(x, -y, z);
                        if (loc.getWorld() != null && loc.getBlockY() > -64) {
                            miningQueue.offer(loc);
                        }
                    }
                }
            }
        }
        
        // Перемешиваем для более естественной добычи
        List<Location> list = new ArrayList<>(miningQueue);
        Collections.shuffle(list);
        miningQueue = new LinkedList<>(list);
    }
    
    private void collectDrops(Location location, Collection<ItemStack> drops) {
        // Ищем ближайшие сундуки или хопперы для складирования
        Location chestLoc = findNearbyStorage();
        
        if (chestLoc != null) {
            // Складываем в сундук
            Block chest = chestLoc.getBlock();
            if (chest.getState() instanceof org.bukkit.block.Container) {
                org.bukkit.block.Container container = (org.bukkit.block.Container) chest.getState();
                for (ItemStack drop : drops) {
                    container.getInventory().addItem(drop);
                }
            }
        } else {
            // Выбрасываем рядом с автошахтером
            for (ItemStack drop : drops) {
                location.getWorld().dropItemNaturally(this.location.clone().add(0, 1, 0), drop);
            }
        }
    }
    
    private Location findNearbyStorage() {
        World world = location.getWorld();
        if (world == null) return null;
        
        // Ищем в радиусе 10 блоков
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    Location loc = location.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    if (block.getType() == Material.CHEST || 
                        block.getType() == Material.HOPPER ||
                        block.getType() == Material.BARREL) {
                        return loc;
                    }
                }
            }
        }
        
        return null;
    }
    
    private void showStartupEffects() {
        World world = location.getWorld();
        if (world == null) return;
        
        // Частицы запуска
        world.spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.GLOW, location.clone().add(0.5, 1, 0.5), 10, 0.2, 0.2, 0.2, 0.05);
        
        // Звук
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
    }
    
    private void showShutdownEffects() {
        World world = location.getWorld();
        if (world == null) return;
        
        // Частицы остановки
        world.spawnParticle(Particle.SMOKE, location.clone().add(0.5, 1, 0.5), 15, 0.3, 0.3, 0.3, 0.05);
        world.spawnParticle(Particle.ASH, location.clone().add(0.5, 1, 0.5), 10, 0.2, 0.2, 0.2, 0.02);
    }
    
    private void showMiningEffects(Location targetLoc, Material blockType) {
        World world = targetLoc.getWorld();
        if (world == null) return;
        
        // Частицы добычи
        world.spawnParticle(Particle.BLOCK, targetLoc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1, blockType.createBlockData());
        
        // Звук добычи
        if (blockType.name().contains("_ORE")) {
            world.playSound(targetLoc, Sound.BLOCK_STONE_BREAK, 0.5f, 1.2f);
            // Дополнительные эффекты для руд
            world.spawnParticle(Particle.HAPPY_VILLAGER, targetLoc.clone().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.1);
        } else {
            world.playSound(targetLoc, Sound.BLOCK_STONE_BREAK, 0.3f, 1.0f);
        }
    }
    
    public void showWorkingArea(Player player) {
        World world = location.getWorld();
        if (world == null) return;
        
        Location center = location.clone().subtract(0, 1, 0);
        
        // Показываем границы рабочей области
        for (int y = 0; y < depth; y++) {
            for (int angle = 0; angle < 360; angle += 10) {
                double x = Math.cos(Math.toRadians(angle)) * radius;
                double z = Math.sin(Math.toRadians(angle)) * radius;
                
                Location particleLoc = center.clone().add(x, -y, z);
                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
            }
        }
        
        player.sendMessage(String.format("§e📐 Рабочая область: %dx%dx%d блоков", radius*2+1, depth, radius*2+1));
    }
    
    // Геттеры и сеттеры
    public int getRadius() { return radius; }
    public void setRadius(int radius) { 
        this.radius = Math.max(1, Math.min(10, radius)); 
        generateMiningQueue();
    }
    
    public int getDepth() { return depth; }
    public void setDepth(int depth) { 
        this.depth = Math.max(1, Math.min(20, depth)); 
        generateMiningQueue();
    }
    
    public MiningMode getMode() { return mode; }
    public void setMode(MiningMode mode) { 
        this.mode = mode; 
        generateMiningQueue();
    }
    
    public boolean isActive() { return isActive; }
    public int getCurrentEnergy() { return currentEnergy; }
    public int getEnergyCapacity() { return energyCapacity; }
    public int getBlocksMinedToday() { return blocksMinedToday; }
    public int getTotalBlocksMined() { return totalBlocksMined; }
    public Map<Material, Integer> getMinedResources() { return minedResources; }
    public Location getLocation() { return location.clone(); }
    public UUID getOwnerId() { return ownerId; }
    
    public Set<Material> getAllowedBlocks() { return allowedBlocks; }
    public Set<Material> getBlacklistedBlocks() { return blacklistedBlocks; }
    
    public long getUptime() {
        return isActive ? System.currentTimeMillis() - operationTime : 0;
    }
    
    public int getQueueSize() {
        return miningQueue.size();
    }
    
    // EnergyConsumer implementation
    @Override
    public int getEnergyRequired() {
        return isActive ? energyPerBlock : 0;
    }
    
    @Override
    public void receiveEnergy(int amount) {
        currentEnergy = Math.min(energyCapacity, currentEnergy + amount);
    }
    
    @Override
    public boolean hasEnoughEnergy() {
        return currentEnergy >= energyPerBlock;
    }
    
    public enum MiningMode {
        SELECTIVE("Выборочная добыча"),
        ORES_ONLY("Только руды"),
        ALL_BLOCKS("Все блоки");
        
        private final String displayName;
        
        MiningMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
