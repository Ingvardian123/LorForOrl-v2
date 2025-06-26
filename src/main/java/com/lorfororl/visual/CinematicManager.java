package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;

public class CinematicManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, CinematicSequence> activeSequences;
    
    public CinematicManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeSequences = new HashMap<>();
    }
    
    public void playLaboratoryActivation(Player player, Location labLocation) {
        CinematicSequence sequence = new CinematicSequence(player, 10); // 10 секунд
        
        sequence.addFrame(0, () -> {
            player.sendTitle("§b§lЛАБОРАТОРИЯ", "§7Инициализация систем...", 10, 40, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.8f);
        });
        
        sequence.addFrame(2, () -> {
            createCircularParticleEffect(labLocation.add(0, 2, 0), Particle.ELECTRIC_SPARK, 30, 3.0);
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.2f);
        });
        
        sequence.addFrame(4, () -> {
            player.sendTitle("§a§lСИСТЕМЫ ОНЛАЙН", "§2Лаборатория готова к работе", 10, 40, 10);
            createSpiralParticleEffect(labLocation, Particle.ENCHANT, 50, 4.0, 3.0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
        
        sequence.addFrame(6, () -> {
            createExplosionEffect(labLocation, Particle.FIREWORK, 100);
            player.getWorld().playSound(labLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.5f);
        });
        
        sequence.addFrame(8, () -> {
            player.sendActionBar("§a§lЛаборатория успешно активирована!");
        });
        
        playSequence(sequence);
    }
    
    public void playReactorMeltdown(Player player, Location reactorLocation) {
        CinematicSequence sequence = new CinematicSequence(player, 15); // 15 секунд
        
        sequence.addFrame(0, () -> {
            player.sendTitle("§4§l⚠ ВНИМАНИЕ ⚠", "§cОбнаружен перегрев реактора!", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 0.5f);
        });
        
        sequence.addFrame(2, () -> {
            createPulsingEffect(reactorLocation, Particle.LAVA, 20, 5.0);
            player.getWorld().playSound(reactorLocation, Sound.BLOCK_FIRE_AMBIENT, 2.0f, 0.8f);
        });
        
        sequence.addFrame(5, () -> {
            player.sendTitle("§4§lКРИТИЧЕСКАЯ ТЕМПЕРАТУРА", "§cНачинается расплавление активной зоны!", 10, 60, 10);
            createShockwaveEffect(reactorLocation, Particle.EXPLOSION_EMITTER, 3, 8.0);
        });
        
        sequence.addFrame(8, () -> {
            // Эффект землетрясения
            shakePlayer(player, 3.0);
            player.getWorld().playSound(reactorLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
        });
        
        sequence.addFrame(10, () -> {
            player.sendTitle("§4§lЭВАКУАЦИЯ!", "§cПокиньте зону немедленно!", 10, 60, 10);
            createRadiationEffect(reactorLocation, 25.0);
        });
        
        sequence.addFrame(12, () -> {
            // Финальный взрыв
            reactorLocation.getWorld().createExplosion(reactorLocation, 20.0f, true, true);
            createNuclearExplosionEffect(reactorLocation);
        });
        
        playSequence(sequence);
    }
    
    public void playResearchComplete(Player player, String researchName) {
        CinematicSequence sequence = new CinematicSequence(player, 8);
        
        sequence.addFrame(0, () -> {
            player.sendTitle("§b§lИССЛЕДОВАНИЕ ЗАВЕРШЕНО!", "", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
        
        sequence.addFrame(2, () -> {
            player.sendTitle("§e§l" + researchName.toUpperCase(), "§aНовые технологии разблокированы!", 10, 60, 10);
            createCelebrationEffect(player.getLocation());
        });
        
        sequence.addFrame(5, () -> {
            player.sendActionBar("§a§l✓ Исследование добавлено в базу знаний");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        });
        
        playSequence(sequence);
    }
    
    public void playPowerArmorActivation(Player player) {
        CinematicSequence sequence = new CinematicSequence(player, 6);
        
        sequence.addFrame(0, () -> {
            player.sendTitle("§6§lСИЛОВАЯ БРОНЯ", "§eИнициализация систем...", 5, 30, 5);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 0.8f);
        });
        
        sequence.addFrame(1, () -> {
            createArmorActivationEffect(player);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        });
        
        sequence.addFrame(3, () -> {
            player.sendTitle("§a§lСИСТЕМЫ ОНЛАЙН", "§2Броня готова к использованию", 5, 30, 5);
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.2f);
        });
        
        sequence.addFrame(5, () -> {
            updateArmorHUD(player, "§6⚡ Силовая броня активирована | Режим: §aNORMAL");
        });
        
        playSequence(sequence);
    }

    private void updateArmorHUD(Player player, String hud) {
        player.sendActionBar(Component.text(hud));
    }
    
    private void playSequence(CinematicSequence sequence) {
        activeSequences.put(sequence.getPlayer().getUniqueId(), sequence);
        sequence.start();
    }
    
    private void createCircularParticleEffect(Location center, Particle particle, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0.1);
        }
    }
    
    private void createSpiralParticleEffect(Location center, Particle particle, int count, double radius, double height) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= count) {
                    cancel();
                    return;
                }
                
                double angle = 2 * Math.PI * tick / 10;
                double currentRadius = radius * (1.0 - (double) tick / count);
                double currentHeight = height * tick / count;
                
                double x = center.getX() + currentRadius * Math.cos(angle);
                double z = center.getZ() + currentRadius * Math.sin(angle);
                double y = center.getY() + currentHeight;
                
                Location particleLoc = new Location(center.getWorld(), x, y, z);
                center.getWorld().spawnParticle(particle, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createExplosionEffect(Location center, Particle particle, int count) {
        center.getWorld().spawnParticle(particle, center, count, 2, 2, 2, 0.3);
    }
    
    private void createPulsingEffect(Location center, Particle particle, int count, double maxRadius) {
        new BukkitRunnable() {
            private int tick = 0;
            private final int maxTicks = 60; // 3 секунды
            
            @Override
            public void run() {
                if (tick >= maxTicks) {
                    cancel();
                    return;
                }
                
                double progress = (double) tick / maxTicks;
                double radius = maxRadius * Math.sin(progress * Math.PI * 4); // 4 пульса
                
                createCircularParticleEffect(center, particle, count, Math.abs(radius));
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createShockwaveEffect(Location center, Particle particle, int waves, double maxRadius) {
        new BukkitRunnable() {
            private int wave = 0;
            
            @Override
            public void run() {
                if (wave >= waves) {
                    cancel();
                    return;
                }
                
                double radius = maxRadius * (wave + 1) / waves;
                createCircularParticleEffect(center, particle, 50, radius);
                center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                
                wave++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    
    private void createRadiationEffect(Location center, double radius) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 100) {
                    cancel();
                    return;
                }
                
                // Создаем радиационные частицы
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double distance = Math.random() * radius;
                    double x = center.getX() + distance * Math.cos(angle);
                    double z = center.getZ() + distance * Math.sin(angle);
                    double y = center.getY() + Math.random() * 10;
                    
                    Location particleLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void createNuclearExplosionEffect(Location center) {
        // Создаем грибовидное облако
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 200) {
                    cancel();
                    return;
                }
                
                // Ствол гриба
                for (int y = 0; y < 20; y++) {
                    Location stemLoc = center.clone().add(0, y, 0);
                    center.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, stemLoc, 5, 1, 0, 1, 0.1);
                }
                
                // Шапка гриба
                for (int i = 0; i < 30; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = 5 + Math.random() * 10;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    double y = center.getY() + 20 + Math.random() * 5;
                    
                    Location cloudLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, cloudLoc, 3, 2, 2, 2, 0.1);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void createCelebrationEffect(Location center) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 60) {
                    cancel();
                    return;
                }
                
                // Фейерверк
                for (int i = 0; i < 10; i++) {
                    double x = center.getX() + (Math.random() - 0.5) * 6;
                    double y = center.getY() + Math.random() * 4 + 2;
                    double z = center.getZ() + (Math.random() - 0.5) * 6;
                    
                    Location fireworkLoc = new Location(center.getWorld(), x, y, z);
                    center.getWorld().spawnParticle(Particle.FIREWORK, fireworkLoc, 5, 0.5, 0.5, 0.5, 0.1);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
    
    private void createArmorActivationEffect(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 40) {
                    cancel();
                    return;
                }
                
                // Энергетические кольца вокруг игрока
                double radius = 2.0 - (tick * 0.05);
                createCircularParticleEffect(center, Particle.ELECTRIC_SPARK, 20, Math.max(0.5, radius));
                
                // Вертикальные энергетические потоки
                for (int y = 0; y < 3; y++) {
                    Location energyLoc = center.clone().add(0, y - 1, 0);
                    center.getWorld().spawnParticle(Particle.ENCHANT, energyLoc, 3, 0.3, 0.1, 0.3, 0.1);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void shakePlayer(Player player, double intensity) {
        new BukkitRunnable() {
            private int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 60) { // 3 секунды
                    cancel();
                    return;
                }
                
                // Создаем эффект тряски изменением направления взгляда
                float yawOffset = (float) ((Math.random() - 0.5) * intensity);
                float pitchOffset = (float) ((Math.random() - 0.5) * intensity);
                
                Location newLoc = player.getLocation();
                newLoc.setYaw(newLoc.getYaw() + yawOffset);
                newLoc.setPitch(Math.max(-90, Math.min(90, newLoc.getPitch() + pitchOffset)));
                
                player.teleport(newLoc);
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    public void stopSequence(Player player) {
        CinematicSequence sequence = activeSequences.remove(player.getUniqueId());
        if (sequence != null) {
            sequence.stop();
        }
    }
    
    public void shutdown() {
        for (CinematicSequence sequence : activeSequences.values()) {
            sequence.stop();
        }
        activeSequences.clear();
    }
    
    private static class CinematicSequence {
        private final Player player;
        private final int duration;
        private final Map<Integer, Runnable> frames;
        private BukkitRunnable task;
        
        public CinematicSequence(Player player, int duration) {
            this.player = player;
            this.duration = duration;
            this.frames = new HashMap<>();
        }
        
        public void addFrame(int second, Runnable action) {
            frames.put(second, action);
        }
        
        public void start() {
            task = new BukkitRunnable() {
                private int tick = 0;
                
                @Override
                public void run() {
                    int second = tick / 20;
                    
                    if (second >= duration) {
                        cancel();
                        return;
                    }
                    
                    Runnable frame = frames.get(second);
                    if (frame != null && tick % 20 == 0) {
                        frame.run();
                    }
                    
                    tick++;
                }
            };
            task.runTaskTimer(LorForOrlPlugin.getInstance(), 0L, 1L);
        }
        
        public void stop() {
            if (task != null) {
                task.cancel();
            }
        }
        
        public Player getPlayer() {
            return player;
        }
    }
}
