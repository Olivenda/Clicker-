package com.clicker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Adds passive and automated click income to the game state.
 */
public class PassiveIncomeManager {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final GameState gameState;

    public PassiveIncomeManager(GameState gameState) {
        this.gameState = gameState;
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            double income = gameState.getPassiveIncome() + gameState.getAutoClickPerSecond() * gameState.getClickValue();
            gameState.addResources(income);
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
