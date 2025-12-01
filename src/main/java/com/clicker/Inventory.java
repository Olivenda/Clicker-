package com.clicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the owned cosmetic skins and offers minimal synchronization hooks for Steam inventory.
 */
public class Inventory {
    private final List<Skin> skins = new ArrayList<>();

    public synchronized void addSkin(Skin skin) {
        skins.add(skin);
    }

    public synchronized List<Skin> getSkins() {
        return Collections.unmodifiableList(new ArrayList<>(skins));
    }

    public synchronized void setSkins(List<Skin> updated) {
        skins.clear();
        skins.addAll(updated);
    }
}
