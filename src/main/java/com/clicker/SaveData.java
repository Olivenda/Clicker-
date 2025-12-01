package com.clicker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serializable data structure for storing savegame information.
 */
public class SaveData {
    public double resources;
    public double clickValue;
    public double passiveIncome;
    public double autoClickPerSecond;
    public Instant lastSave;
    public List<Skin> skins = new ArrayList<>();
    public List<UpgradeSnapshot> upgrades = new ArrayList<>();

    public static class UpgradeSnapshot {
        public String id;
        public int quantity;
    }

    public static SaveData from(GameState state, Inventory inventory, List<Upgrade> availableUpgrades) {
        SaveData data = new SaveData();
        data.resources = state.getResources();
        data.clickValue = state.getClickValue();
        data.passiveIncome = state.getPassiveIncome();
        data.autoClickPerSecond = state.getAutoClickPerSecond();
        data.lastSave = Instant.now();
        data.skins.addAll(inventory.getSkins());
        Map<String, Integer> quantities = state.getUpgradeQuantities();
        for (Upgrade upgrade : availableUpgrades) {
            UpgradeSnapshot snapshot = new UpgradeSnapshot();
            snapshot.id = upgrade.getId();
            snapshot.quantity = quantities.getOrDefault(upgrade.getId(), upgrade.getQuantity());
            data.upgrades.add(snapshot);
        }
        return data;
    }
}
