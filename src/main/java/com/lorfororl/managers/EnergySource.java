package com.lorfororl.managers;

import org.bukkit.Location;

public interface EnergySource {
    
    /**
     * Генерирует энергию за тик
     * @return количество энергии
     */
    int generateEnergy();
    
    /**
     * Проверяет, работает ли источник энергии
     * @return true если работает
     */
    boolean isOperational();
    
    /**
     * Получает местоположение источника энергии
     * @return локация
     */
    Location getLocation();
    
    /**
     * Получает максимальную выработку энергии
     * @return максимальная выработка
     */
    default int getMaxEnergyOutput() {
        return 100;
    }
    
    /**
     * Получает эффективность источника (0.0 - 1.0)
     * @return эффективность
     */
    default double getEfficiency() {
        return 1.0;
    }
    
    /**
     * Получает тип источника энергии
     * @return тип источника
     */
    default String getEnergySourceType() {
        return "unknown";
    }
}
