package com.lorfororl.managers;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoundManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Long> lastSoundTime;
    private final Map<String, SoundSequence> activeSoundSequences;
    
    public SoundManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.lastSoundTime = new HashMap<>();
        this.activeSoundSequences = new HashMap<>();
    }
    
    public void shutdown() {
        // Останавливаем все звуковые последовательности
        for (SoundSequence sequence : activeSoundSequences.values()) {
            sequence.stop();
        }
        activeSoundSequences.clear();
        lastSoundTime.clear();
    }
    
    public void playSound(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() == null) return;
        
        try {
            location.getWorld().playSound(location, sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при воспроизведении звука: " + e.getMessage());
        }
    }
    
    public void playSound(Player player, Sound sound, float volume, float pitch) {
        if (!player.isOnline()) return;
        
        try {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при воспроизведении звука для игрока: " + e.getMessage());
        }
    }
    
    public void playSoundWithCooldown(Player player, Sound sound, float volume, float pitch, long cooldownMs) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        Long lastTime = lastSoundTime.get(playerId);
        if (lastTime != null && currentTime - lastTime < cooldownMs) {
            return; // Звук еще на кулдауне
        }
        
        playSound(player, sound, volume, pitch);
        lastSoundTime.put(playerId, currentTime);
    }
    
    // Звуки для различных систем
    public void playGeigerSound(Player player, double radiationLevel) {
        if (radiationLevel <= 0) return;
        
        // Частота и высота звука зависят от уровня радиации
        float pitch = (float) Math.min(2.0, 0.5 + (radiationLevel / 25.0));
        long cooldown = Math.max(100, (long)(1000 / radiationLevel)); // Чем больше радиация, тем чаще звук
        
        playSoundWithCooldown(player, Sound.BLOCK_NOTE_BLOCK_CLICK, 0.3f, pitch, cooldown);
    }
    
    public void playEnergySound(Player player, boolean charging) {
        if (charging) {
            playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.5f);
        } else {
            playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 0.8f);
        }
    }
    
    public void playMachineSound(Location location, String machineType) {
        switch (machineType.toLowerCase()) {
            case "centrifuge":
                playSound(location, Sound.BLOCK_PISTON_EXTEND, 0.8f, 0.8f);
                // Дополнительный звук вращения
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playSound(location, Sound.BLOCK_PISTON_CONTRACT, 0.6f, 1.2f);
                    }
                }.runTaskLater(plugin, 10L);
                break;
                
            case "reactor":
                playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.6f, 0.5f);
                // Низкочастотный гул
                playSound(location, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.3f, 0.3f);
                break;
                
            case "laboratory":
                playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 0.7f, 1.2f);
                // Звуки оборудования
                playSound(location, Sound.BLOCK_DISPENSER_DISPENSE, 0.4f, 1.5f);
                break;
                
            case "autominer":
                playSound(location, Sound.BLOCK_PISTON_CONTRACT, 0.9f, 1.0f);
                // Звук копания
                playSound(location, Sound.BLOCK_GRAVEL_BREAK, 0.5f, 0.8f);
                break;
                
            case "solar_panel":
                playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4f, 2.0f);
                break;
                
            case "generator":
                playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 0.5f);
                playSound(location, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.6f, 1.0f);
                break;
                
            default:
                playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
        }
    }
    
    public void playWeaponSound(Location location, String weaponType) {
        switch (weaponType.toLowerCase()) {
            case "railgun":
                // Последовательность звуков для рельсотрона
                playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f); // Зарядка
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.5f); // Выстрел
                        playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 2.0f); // Взрыв
                    }
                }.runTaskLater(plugin, 5L);
                break;
                
            case "laser":
                playSound(location, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 2.0f);
                playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
                break;
                
            case "plasma":
                playSound(location, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
                playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.8f);
                break;
                
            case "nuclear_bomb":
                // Последовательность для ядерного взрыва
                playNuclearExplosionSequence(location);
                break;
                
            default:
                playSound(location, Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        }
    }
    
    private void playNuclearExplosionSequence(Location location) {
        String sequenceId = "nuclear_explosion_" + location.hashCode() + "_" + System.currentTimeMillis();
        
        SoundSequence sequence = new SoundSequence(sequenceId);
        
        // Предупреждающий звук
        sequence.addSound(0, location, Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 0.5f);
        sequence.addSound(20, location, Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 0.3f);
        
        // Основной взрыв
        sequence.addSound(40, location, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.1f);
        sequence.addSound(45, location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.2f);
        sequence.addSound(50, location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.1f);
        
        // Эхо
        sequence.addSound(80, location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.3f);
        sequence.addSound(120, location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        
        activeSoundSequences.put(sequenceId, sequence);
        sequence.start();
    }
    
    public void playAlarmSound(Location location, int level) {
        Sound sound;
        float pitch;
        float volume = 1.0f;
        
        switch (level) {
            case 1: // Low alert
                sound = Sound.BLOCK_NOTE_BLOCK_BELL;
                pitch = 1.0f;
                break;
            case 2: // Medium alert
                sound = Sound.BLOCK_NOTE_BLOCK_BELL;
                pitch = 1.5f;
                volume = 1.5f;
                break;
            case 3: // High alert
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                pitch = 2.0f;
                volume = 2.0f;
                break;
            default:
                sound = Sound.BLOCK_NOTE_BLOCK_BELL;
                pitch = 1.0f;
        }
        
        playSound(location, sound, volume, pitch);
        
        // Для высокого уровня тревоги добавляем дополнительные звуки
        if (level >= 2) {
            new BukkitRunnable() {
                int count = 0;
                
                @Override
                public void run() {
                    if (count >= 3) {
                        cancel();
                        return;
                    }
                    
                    playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, volume * 0.7f, pitch * 1.2f);
                    count++;
                }
            }.runTaskTimer(plugin, 5L, 5L);
        }
    }
    
    public void playResearchSound(Player player, boolean completed) {
        if (completed) {
            playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
                }
            }.runTaskLater(plugin, 10L);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                }
            }.runTaskLater(plugin, 20L);
        } else {
            playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 0.8f);
        }
    }
    
    public void playTeleportSound(Location location) {
        playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
            }
        }.runTaskLater(plugin, 5L);
    }
    
    public void playErrorSound(Player player) {
        playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
    
    public void playSuccessSound(Player player) {
        playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.5f);
            }
        }.runTaskLater(plugin, 3L);
    }
    
    public void playWarningSound(Player player) {
        playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }
    
    public void playNotificationSound(Player player, String type) {
        switch (type.toLowerCase()) {
            case "achievement":
                playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                break;
            case "message":
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                break;
            case "warning":
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                break;
            case "error":
                playErrorSound(player);
                break;
            case "success":
                playSuccessSound(player);
                break;
            default:
                playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
        }
    }
    
    public void playAmbientSound(Location location, String environment) {
        switch (environment.toLowerCase()) {
            case "laboratory":
                playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 0.3f, 0.8f);
                break;
            case "reactor":
                playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.4f, 0.5f);
                break;
            case "radiation_zone":
                playSound(location, Sound.ENTITY_WITCH_AMBIENT, 0.2f, 0.7f);
                break;
            case "energy_facility":
                playSound(location, Sound.BLOCK_CONDUIT_AMBIENT, 0.3f, 1.2f);
                break;
        }
    }
    
    public void stopAllSounds(Player player) {
        // Minecraft не предоставляет прямого способа остановить все звуки
        // Но мы можем воспроизвести тишину или очистить наши кулдауны
        lastSoundTime.remove(player.getUniqueId());
    }
    
    // Класс для последовательности звуков
    private class SoundSequence {
        private final String id;
        private final Map<Integer, SoundEvent> sounds;
        private BukkitRunnable task;
        private boolean running = false;
        
        public SoundSequence(String id) {
            this.id = id;
            this.sounds = new HashMap<>();
        }
        
        public void addSound(int tick, Location location, Sound sound, float volume, float pitch) {
            sounds.put(tick, new SoundEvent(location, sound, volume, pitch));
        }
        
        public void start() {
            if (running) return;
            
            running = true;
            task = new BukkitRunnable() {
                private int currentTick = 0;
                
                @Override
                public void run() {
                    SoundEvent event = sounds.get(currentTick);
                    if (event != null) {
                        playSound(event.location, event.sound, event.volume, event.pitch);
                    }
                    
                    currentTick++;
                    
                    // Останавливаемся, когда все звуки воспроизведены
                    if (currentTick > sounds.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 20) {
                        stop();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        
        public void stop() {
            running = false;
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
            activeSoundSequences.remove(id);
        }
        
        private class SoundEvent {
            final Location location;
            final Sound sound;
            final float volume;
            final float pitch;
            
            SoundEvent(Location location, Sound sound, float volume, float pitch) {
                this.location = location;
                this.sound = sound;
                this.volume = volume;
                this.pitch = pitch;
            }
        }
    }
}
