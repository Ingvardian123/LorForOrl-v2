package com.lorfororl.config;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final LorForOrlPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public void saveConfig() {
        plugin.saveConfig();
    }
    
    // Радиация
    public double getRadiationDecayRate() {
        return config.getDouble("radiation.natural-decay-rate", 0.01);
    }
    
    public double getMaxRadiationLevel() {
        return config.getDouble("radiation.max-radiation-level", 10.0);
    }
    
    public double getNauseaThreshold() {
        return config.getDouble("radiation.effects.nausea-threshold", 0.5);
    }
    
    public double getWitherThreshold() {
        return config.getDouble("radiation.effects.wither-threshold", 1.5);
    }
    
    public double getBlindnessThreshold() {
        return config.getDouble("radiation.effects.blindness-threshold", 3.0);
    }
    
    public double getDeathThreshold() {
        return config.getDouble("radiation.effects.death-threshold", 8.0);
    }
    
    public double getHazmatProtection() {
        return config.getDouble("radiation.protection.hazmat-suit", 0.90);
    }
    
    public double getPowerArmorProtection() {
        return config.getDouble("radiation.protection.power-armor", 0.95);
    }
    
    // Энергия
    public int getMaxPlayerEnergy() {
        return config.getInt("energy.max-player-energy", 1000);
    }
    
    public int getMaxReactorOutput() {
        return config.getInt("energy.max-reactor-output", 1000);
    }
    
    public int getPowerArmorConsumption(String mode) {
        return config.getInt("energy.power-armor-consumption." + mode.toLowerCase(), 1);
    }
    
    public int getRailgunConsumption(String mode) {
        return config.getInt("energy.railgun-consumption." + mode.toLowerCase(), 50);
    }
    
    // Реактор
    public long getFuelConsumptionInterval() {
        return config.getLong("reactor.fuel-consumption-interval", 300000);
    }
    
    public int getMaxFuelLevel() {
        return config.getInt("reactor.max-fuel-level", 64);
    }
    
    public int getBaseEnergyPerFuel() {
        return config.getInt("reactor.base-energy-per-fuel", 50);
    }
    
    public int getMaxTemperature() {
        return config.getInt("reactor.max-temperature", 1000);
    }
    
    public int getOverheatThreshold() {
        return config.getInt("reactor.overheat-threshold", 800);
    }
    
    public int getCriticalThreshold() {
        return config.getInt("reactor.critical-threshold", 950);
    }
    
    public float getExplosionPower() {
        return (float) config.getDouble("reactor.explosion-power", 20.0);
    }
    
    // Центрифуга
    public long getCentrifugeWorkDuration() {
        return config.getLong("centrifuge.work-duration", 1560000);
    }
    
    public double getDustChance(int amount) {
        return config.getDouble("centrifuge.dust-chances." + amount + "-dust", 0.0);
    }
    
    // Исследования
    public double getResearchStationBonus() {
        return config.getDouble("research.station-speed-bonus", 0.5);
    }
    
    public int getResearchTime(String researchId) {
        return config.getInt("research.times." + researchId.replace("_", "-"), 60);
    }
    
    // Строительство
    public int getBuildTime(String structureType) {
        return config.getInt("building.build-times." + structureType.toLowerCase().replace("_", "-"), 60);
    }
    
    // Оружие
    public long getRailgunCooldown(String mode) {
        return config.getLong("weapons.railgun.cooldowns." + mode.toLowerCase(), 3000);
    }
    
    public double getRailgunDamage(String mode) {
        return config.getDouble("weapons.railgun.damage." + mode.toLowerCase(), 15.0);
    }
    
    public int getRailgunRange(String mode) {
        return config.getInt("weapons.railgun.range." + mode.toLowerCase(), 100);
    }
    
    // Производительность
    public int getMaxCentrifugesPerPlayer() {
        return config.getInt("performance.max-centrifuges-per-player", 5);
    }
    
    public int getMaxReactorsPerPlayer() {
        return config.getInt("performance.max-reactors-per-player", 2);
    }
    
    public int getMaxLaboratoriesPerPlayer() {
        return config.getInt("performance.max-laboratories-per-player", 3);
    }
    
    public int getUpdateInterval(String system) {
        return config.getInt("performance.update-intervals." + system, 20);
    }
    
    // Безопасность
    public boolean isStructureProtectionEnabled() {
        return config.getBoolean("security.protect-structures", true);
    }
    
    public boolean isOwnerOnlyAccess() {
        return config.getBoolean("security.owner-only-access", true);
    }
    
    public long getBuildCooldown() {
        return config.getLong("security.cooldown-between-builds", 30000);
    }
    
    // Экономика
    public double getItemPrice(String item) {
        return config.getDouble("economy.prices." + item.replace("_", "-"), 0.0);
    }
    
    public double getResearchReward(String research) {
        return config.getDouble("economy.research-rewards." + research.replace("_", "-"), 0.0);
    }
    
    // Общие настройки
    public boolean isDebugEnabled() {
        return config.getBoolean("plugin.debug", false);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("plugin.auto-save-interval", 300);
    }
}
