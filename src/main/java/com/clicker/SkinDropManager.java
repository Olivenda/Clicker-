package com.clicker;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically drops a cosmetic skin to the player inventory.
 */
public class SkinDropManager {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Inventory inventory;
    private final Random random = new Random();
    private DropListener listener;

    public interface DropListener {
        void onSkinDropped(Skin skin);
    }

    public SkinDropManager(Inventory inventory, DropListener listener) {
        this.inventory = inventory;
        this.listener = listener;
    }

    public void setListener(DropListener listener) {
        this.listener = listener;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::drop, 60, 60, TimeUnit.SECONDS);
    }

    private void drop() {
        Skin skin = randomSkin();
        inventory.addSkin(skin);
        if (listener != null) {
            listener.onSkinDropped(skin);
        }
    }

    private Skin randomSkin() {
        double roll = random.nextDouble();
        double cumulative = 0;
        for (SkinRarity rarity : SkinRarity.values()) {
            cumulative += rarity.getDropChance();
            if (roll <= cumulative) {
                return new Skin(rarity.name() + " Skin", rarity, "icons/" + rarity.name().toLowerCase() + ".png");
            }
        }
        return new Skin("Common Skin", SkinRarity.COMMON, "icons/common.png");
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
