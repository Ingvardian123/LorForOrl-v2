package com.lorfororl.weapons;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class NuclearBomb {
    
    private final String id;
    private final Location location;
    private final Player owner;
    private final int explosionPower;
    private final int radiationRadius;
    private final LorForOrlPlugin plugin;
    private BukkitRunnable countdownTask;
    private int countdown;
    private boolean armed;
    private boolean exploded;
    
    public NuclearBomb(String id, Location location, Player owner, int explosionPower, int radiationRadius, LorForOrlPlugin plugin) {
        this.id = id;
        this.location = location;
        this.owner = owner;
        this.explosionPower = explosionPower;
        this.radiationRadius = radiationRadius;
        this.plugin = plugin;
        this.countdown = 300; // 15 секунд (300 тиков)
        this.armed = false;
        this.exploded = false;
    }
    
    public void arm() {
        if (armed || exploded) return;
        
        armed = true;
        
        // Размещаем физический блок бомбы
        location.getBlock().setType(Material.TNT);
        
        // Уведомления
        if (owner != null) {
            owner.sendMessage("§4💣 ЯДЕРНАЯ БОМБА АКТИВИРОВАНА!");
            owner.sendMessage("§c⚠ ВРЕМЯ ДО ВЗРЫВА: 15 СЕКУНД!");
            owner.sendMessage("§e💡 Используйте /lorfororl bomb defuse " + id + " для обезвреживания");
        }
        
        // Предупреждение всем игрокам в радиусе
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 100) {
                player.sendTitle("§4§l⚠ ЯДЕРНАЯ УГРОЗА ⚠", 
                    "§cОбнаружена активная ядерная бомба! Эвакуируйтесь!", 
                    10, 100, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            }
        }
        
        startCountdown();
    }
    
    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (exploded || !armed) {
                    cancel();
                    return;
                }
                
                countdown--;
                
                // Визуальные и звуковые эффекты каждую секунду
                if (countdown % 20 == 0) {
                    int secondsLeft = countdown / 20;
                    
                    // Звуковые сигналы
                    location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 0.5f);
                    
                    // Частицы предупреждения
                    location.getWorld().spawnParticle(Particle.LAVA, location.add(0, 2, 0), 20, 2, 2, 2, 0.1);
                    location.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, location.add(0, 3, 0), 10, 1, 1, 1, 0.1);
                    
                    // Уведомления игрокам
                    for (Player player : location.getWorld().getPlayers()) {
                        if (player.getLocation().distance(location) <= 200) {
                            player.sendActionBar(String.format("§4💣 ЯДЕРНАЯ БОМБА: §c%d §4СЕКУНД ДО ВЗРЫВА!", secondsLeft));
                            
                            if (secondsLeft <= 5) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 2.0f);
                            }
                        }
                    }
                    
                    // Последние 10 секунд - интенсивные эффекты
                    if (secondsLeft <= 10) {
                        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 5, 1, 1, 1, 0.1);
                        
                        // Эффект землетрясения
                        for (Player player : location.getWorld().getPlayers()) {
                            if (player.getLocation().distance(location) <= 50) {
                                shakePlayer(player, 1.0 + (10 - secondsLeft) * 0.2);
                            }
                        }
                    }
                }
                
                // Взрыв!
                if (countdown <= 0) {
                    explode();
                    cancel();
                }
            }
        };
        
        countdownTask.runTaskTimer(plugin, 0L, 1L);
    }
    
    public boolean defuse(Player player) {
        if (!armed || exploded) {
            return false;
        }
        
        if (countdown <= 60) { // Менее 3 секунд - слишком поздно
            player.sendMessage("§c❌ Слишком поздно для обезвреживания!");
            return false;
        }
        
        // Проверяем расстояние
        if (player.getLocation().distance(location) > 5) {
            player.sendMessage("§c❌ Подойдите ближе к бомбе для обезвреживания!");
            return false;
        }
        
        // Шанс успешного обезвреживания зависит от оставшегося времени
        double successChance = Math.min(0.9, countdown / 300.0);
        
        if (Math.random() < successChance) {
            // Успешное обезвреживание
            armed = false;
            location.getBlock().setType(Material.AIR);
            
            if (countdownTask != null) {
                countdownTask.cancel();
            }
            
            player.sendMessage("§a✅ Ядерная бомба успешно обезврежена!");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            
            // Уведомляем всех
            for (Player p : location.getWorld().getPlayers()) {
                if (p.getLocation().distance(location) <= 100) {
                    p.sendTitle("§a§lБОМБА ОБЕЗВРЕЖЕНА", 
                        "§2Ядерная угроза устранена игроком " + player.getName(), 
                        10, 60, 20);
                }
            }
            
            // Достижение
            plugin.getAchievementManager().checkAchievement(player, "bomb_defuser", 1);
            
            return true;
        } else {
            // Неудачная попытка - ускоряем взрыв
            player.sendMessage("§c❌ Обезвреживание не удалось! Бомба взорвется через 3 секунды!");
            countdown = 60; // 3 секунды
            return false;
        }
    }
    
    private void explode() {
        if (exploded) return;
        exploded = true;
        
        // Убираем блок бомбы
        location.getBlock().setType(Material.AIR);
        
        // Создаем мега-взрыв
        createNuclearExplosion();
        
        // Создаем радиационную зону
        plugin.getRadiationZoneManager().createRadiationZone(
            location, 
            radiationRadius, 
            10.0, // Максимальная радиация
            7200000L, // 2 часа
            com.lorfororl.radiation.RadiationZone.RadiationZoneType.NUCLEAR_BOMB
        );
        
        // Статистика
        if (owner != null) {
            plugin.getAchievementManager().checkAchievement(owner, "nuclear_destroyer", 1);
        }
        
        // Логирование
        plugin.getLogger().warning(String.format("Nuclear bomb exploded at %s by %s", 
            locationToString(location), owner != null ? owner.getName() : "Unknown"));
    }
    
    private void createNuclearExplosion() {
        // Основной взрыв
        location.getWorld().createExplosion(location, explosionPower, true, true);
        
        // Дополнительные взрывы для реалистичности
        new BukkitRunnable() {
            private int stage = 0;
            
            @Override
            public void run() {
                if (stage >= 10) {
                    cancel();
                    return;
                }
                
                // Создаем кольцевые взрывы
                double radius = 5 + stage * 3;
                for (int i = 0; i < 8; i++) {
                    double angle = (2 * Math.PI * i) / 8;
                    double x = location.getX() + radius * Math.cos(angle);
                    double z = location.getZ() + radius * Math.sin(angle);
                    Location explosionLoc = new Location(location.getWorld(), x, location.getY(), z);
                    
                    location.getWorld().createExplosion(explosionLoc, explosionPower * 0.7f, true, true);
                }
                
                // Звуковые эффекты
                location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3.0f, 0.3f);
                
                // Грибовидное облако
                createMushroomCloud(stage);
                
                stage++;
            }
        }.runTaskTimer(plugin, 5L, 10L);
        
        // Ударная волна
        createShockwave();
    }
    
    private void createMushroomCloud(int stage) {
        // Ствол гриба
        for (int y = 0; y < 30 + stage * 2; y++) {
            Location stemLoc = location.clone().add(0, y, 0);
            location.getWorld().spawnParticle(Particle.LARGE_SMOKE, stemLoc, 10, 2, 0, 2, 0.1);
            location.getWorld().spawnParticle(Particle.FLAME, stemLoc, 5, 1, 0, 1, 0.1);
        }
        
        // Шапка гриба
        int cloudHeight = 30 + stage * 2;
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = 10 + Math.random() * (15 + stage * 2);
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            double y = location.getY() + cloudHeight + Math.random() * 10;
            
            Location cloudLoc = new Location(location.getWorld(), x, y, z);
            location.getWorld().spawnParticle(Particle.EXPLOSION, cloudLoc, 5, 3, 3, 3, 0.1);
            location.getWorld().spawnParticle(Particle.LARGE_SMOKE, cloudLoc, 8, 2, 2, 2, 0.1);
        }
    }
    
    private void createShockwave() {
        new BukkitRunnable() {
            private int radius = 5;
            private final int maxRadius = explosionPower * 3;
            
            @Override
            public void run() {
                if (radius > maxRadius) {
                    cancel();
                    return;
                }
                
                // Создаем кольцо частиц
                for (int i = 0; i < 50; i++) {
                    double angle = (2 * Math.PI * i) / 50;
                    double x = location.getX() + radius * Math.cos(angle);
                    double z = location.getZ() + radius * Math.sin(angle);
                    Location shockLoc = new Location(location.getWorld(), x, location.getY() + 1, z);
                    
                    location.getWorld().spawnParticle(Particle.EXPLOSION, shockLoc, 3, 0.5, 0.5, 0.5, 0.1);
                }
                
                // Отбрасываем игроков
                for (Player player : location.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(location);
                    if (distance <= radius + 5 && distance >= radius - 5) {
                        // Рассчитываем силу отбрасывания
                        double force = Math.max(0.5, 3.0 - (distance / 20.0));
                        
                        // Направление от взрыва
                        org.bukkit.util.Vector direction = player.getLocation().toVector()
                            .subtract(location.toVector()).normalize();
                        direction.setY(0.5); // Подбрасываем вверх
                        direction.multiply(force);
                        
                        player.setVelocity(direction);
                        player.damage(Math.max(1, 20 - distance));
                        
                        // Радиация
                        plugin.getRadiationManager().addRadiation(player, 5.0 - (distance / 10.0));
                    }
                }
                
                radius += 3;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void shakePlayer(Player player, double intensity) {
        Location loc = player.getLocation();
        float yawOffset = (float) ((Math.random() - 0.5) * intensity * 2);
        float pitchOffset = (float) ((Math.random() - 0.5) * intensity);
        
        loc.setYaw(loc.getYaw() + yawOffset);
        loc.setPitch(Math.max(-90, Math.min(90, loc.getPitch() + pitchOffset)));
        
        player.teleport(loc);
    }
    
    private String locationToString(Location loc) {
        return String.format("%.1f,%.1f,%.1f in %s", 
            loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
    
    // Геттеры
    public String getId() { return id; }
    public Location getLocation() { return location; }
    public Player getOwner() { return owner; }
    public boolean isArmed() { return armed; }
    public boolean isExploded() { return exploded; }
    public int getCountdown() { return countdown; }
    public int getSecondsLeft() { return countdown / 20; }
}
