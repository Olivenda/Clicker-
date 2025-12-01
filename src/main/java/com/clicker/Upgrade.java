package com.clicker;

/**
 * Defines a purchasable upgrade with price scaling and a deterministic effect.
 */
public class Upgrade {
    private final String id;
    private final String name;
    private final String description;
    private final double basePrice;
    private final double priceMultiplier;
    private final UpgradeCategory category;
    private final UpgradeEffectType effectType;
    private final double effectPerPurchase;

    private int quantity;

    public Upgrade(String id,
                   String name,
                   String description,
                   double basePrice,
                   double priceMultiplier,
                   UpgradeCategory category,
                   UpgradeEffectType effectType,
                   double effectPerPurchase) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.priceMultiplier = priceMultiplier;
        this.category = category;
        this.effectType = effectType;
        this.effectPerPurchase = effectPerPurchase;
        this.quantity = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public UpgradeCategory getCategory() {
        return category;
    }

    public UpgradeEffectType getEffectType() {
        return effectType;
    }

    public double getEffectPerPurchase() {
        return effectPerPurchase;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity() {
        quantity++;
    }

    public double currentPrice() {
        return Math.round(basePrice * Math.pow(priceMultiplier, quantity) * 100.0) / 100.0;
    }
}
