package com.clicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages available upgrades and applying purchases to the game state.
 */
public class UpgradeShop {
    private final List<Upgrade> upgrades = new ArrayList<>();
    private final GameState gameState;

    public UpgradeShop(GameState gameState) {
        this.gameState = gameState;
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void addUpgrade(Upgrade upgrade) {
        upgrades.add(upgrade);
    }

    public boolean purchase(String upgradeId) {
        Optional<Upgrade> upgradeOpt = upgrades.stream().filter(u -> u.getId().equals(upgradeId)).findFirst();
        if (upgradeOpt.isEmpty()) {
            return false;
        }
        Upgrade upgrade = upgradeOpt.get();
        double cost = upgrade.currentPrice();
        if (gameState.spendResources(cost)) {
            upgrade.increaseQuantity();
            gameState.applyUpgrade(upgrade);
            return true;
        }
        return false;
    }
}
