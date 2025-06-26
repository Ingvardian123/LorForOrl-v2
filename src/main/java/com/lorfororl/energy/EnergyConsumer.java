package com.lorfororl.energy;

public interface EnergyConsumer {
    int getEnergyRequired();
    void receiveEnergy(int amount);
    boolean hasEnoughEnergy();
}
