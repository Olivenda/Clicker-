package com.clicker;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a cosmetic skin with an immutable ID to allow safe trading and storage.
 */
public class Skin {
    private final String id;
    private final String name;
    private final SkinRarity rarity;
    private final String iconPath;

    public Skin(String name, SkinRarity rarity, String iconPath) {
        this(UUID.randomUUID().toString(), name, rarity, iconPath);
    }

    public Skin(String id, String name, SkinRarity rarity, String iconPath) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.iconPath = iconPath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkinRarity getRarity() {
        return rarity;
    }

    public String getIconPath() {
        return iconPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skin)) return false;
        Skin skin = (Skin) o;
        return Objects.equals(id, skin.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return rarity + " | " + name + " (#" + id.substring(0, Math.min(8, id.length())) + ")";
    }
}
