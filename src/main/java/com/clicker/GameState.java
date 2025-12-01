package com.clicker;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the mutable game state that can be saved, loaded and mutated by gameplay systems.
 */
public class GameState {
    private double resources;
    private double clickValue;
    private double passiveIncome;
    private double autoClickPerSecond;
    private final Map<String, Integer> upgradeQuantities = new HashMap<>();
    private Instant lastSave;

    public GameState() {
        this.resources = 0;
        this.clickValue = 1;
        this.passiveIncome = 0;
        this.autoClickPerSecond = 0;
        this.lastSave = Instant.now();
    }

    public synchronized void addResources(double amount) {
        resources += amount;
    }

    public synchronized boolean spendResources(double cost) {
        if (resources >= cost) {
            resources -= cost;
            return true;
        }
        return false;
    }

    public synchronized double getResources() {
        return resources;
    }

    public synchronized double getClickValue() {
        return clickValue;
    }

    public synchronized double getPassiveIncome() {
        return passiveIncome;
    }

    public synchronized double getAutoClickPerSecond() {
        return autoClickPerSecond;
    }

    public synchronized void applyUpgrade(Upgrade upgrade) {
        switch (upgrade.getEffectType()) {
            case CLICK_VALUE -> clickValue += upgrade.getEffectPerPurchase();
            case PASSIVE_INCOME -> passiveIncome += upgrade.getEffectPerPurchase();
            case AUTO_CLICK -> autoClickPerSecond += upgrade.getEffectPerPurchase();
            default -> throw new IllegalStateException("Unknown effect: " + upgrade.getEffectType());
        }
        upgradeQuantities.put(upgrade.getId(), upgrade.getQuantity());
    }

    public synchronized void syncUpgradeQuantity(String id, int quantity) {
        upgradeQuantities.put(id, quantity);
    }

    public synchronized Map<String, Integer> getUpgradeQuantities() {
        return new HashMap<>(upgradeQuantities);
    }

    public synchronized Instant getLastSave() {
        return lastSave;
    }

    public synchronized void setLastSave(Instant lastSave) {
        this.lastSave = lastSave;
    }

    public synchronized void setResources(double resources) {
        this.resources = resources;
    }

    public synchronized void setClickValue(double clickValue) {
        this.clickValue = clickValue;
    }

    public synchronized void setPassiveIncome(double passiveIncome) {
        this.passiveIncome = passiveIncome;
    }

    public synchronized void setAutoClickPerSecond(double autoClickPerSecond) {
        this.autoClickPerSecond = autoClickPerSecond;
    }
}
