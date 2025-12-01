package com.clicker;

/**
 * Defines rarity tiers for cosmetic skins.
 */
public enum SkinRarity {
    COMMON(0.6),
    UNCOMMON(0.2),
    RARE(0.12),
    EPIC(0.06),
    LEGENDARY(0.02);

    private final double dropChance;

    SkinRarity(double dropChance) {
        this.dropChance = dropChance;
    }

    public double getDropChance() {
        return dropChance;
    }
}
