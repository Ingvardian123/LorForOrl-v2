package com.lorfororl.energy;

public interface EnergySource {
    int getEnergyOutput();
    int getMaxEnergyOutput();
    void consumeEnergy(int amount);
    boolean isActive();
    void setActive(boolean active);
}
