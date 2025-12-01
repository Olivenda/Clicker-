package com.clicker;

/**
 * Handles manual click actions.
 */
public class ClickHandler {
    private final GameState gameState;

    public ClickHandler(GameState gameState) {
        this.gameState = gameState;
    }

    public void click() {
        gameState.addResources(gameState.getClickValue());
    }
}
