package com.clicker;

import javax.swing.*;
import java.io.File;

public class GameApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameState state = new GameState();
            Inventory inventory = new Inventory();
            UpgradeShop shop = new UpgradeShop(state);
            seedUpgrades(shop);

            SaveManager saveManager = new SaveManager();
            File saveFile = new File("savegame.secure");
            try {
                SaveData loaded = saveManager.load(saveFile);
                saveManager.applyLoadedData(loaded, state, inventory, shop.getUpgrades());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClickHandler clickHandler = new ClickHandler(state);
            PassiveIncomeManager passiveIncomeManager = new PassiveIncomeManager(state);
            SkinDropManager skinDropManager = new SkinDropManager(inventory, null);

            UIManager uiManager = new UIManager(state, clickHandler, shop, inventory, saveManager, passiveIncomeManager, skinDropManager, saveFile);
            skinDropManager.setListener(uiManager);
            uiManager.createAndShowUI();
            passiveIncomeManager.start();
            skinDropManager.start();
        });
    }

    private static void seedUpgrades(UpgradeShop shop) {
        shop.addUpgrade(new Upgrade("u-click-1", "Stronger Clicks", "+1 per click", 10, 1.15, UpgradeCategory.CLICK_POWER, UpgradeEffectType.CLICK_VALUE, 1));
        shop.addUpgrade(new Upgrade("u-click-2", "Titanium Finger", "+5 per click", 100, 1.20, UpgradeCategory.CLICK_POWER, UpgradeEffectType.CLICK_VALUE, 5));
        shop.addUpgrade(new Upgrade("u-passive-1", "Solar Panel", "+0.5 income/s", 50, 1.18, UpgradeCategory.PASSIVE_INCOME, UpgradeEffectType.PASSIVE_INCOME, 0.5));
        shop.addUpgrade(new Upgrade("u-passive-2", "Fusion Reactor", "+5 income/s", 500, 1.20, UpgradeCategory.PASSIVE_INCOME, UpgradeEffectType.PASSIVE_INCOME, 5));
        shop.addUpgrade(new Upgrade("u-auto-1", "Automation Drone", "+0.2 auto-click/s", 200, 1.25, UpgradeCategory.AUTOMATION, UpgradeEffectType.AUTO_CLICK, 0.2));
        shop.addUpgrade(new Upgrade("u-auto-2", "Quantum Bot", "+1 auto-click/s", 2000, 1.30, UpgradeCategory.AUTOMATION, UpgradeEffectType.AUTO_CLICK, 1));
    }
}
