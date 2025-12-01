package com.clicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Builds the Swing UI and wires user interactions to the core game systems.
 */
public class UIManager implements SkinDropManager.DropListener {
    private final GameState gameState;
    private final ClickHandler clickHandler;
    private final UpgradeShop shop;
    private final Inventory inventory;
    private final SaveManager saveManager;
    private final PassiveIncomeManager passiveIncomeManager;
    private final SkinDropManager skinDropManager;
    private final File saveFile;

    private JLabel resourceLabel;
    private JLabel passiveLabel;
    private JLabel dropTimerLabel;
    private JLabel statusLabel;
    private DefaultTableModel upgradeModel;
    private DefaultTableModel inventoryModel;
    private long nextDropAt;

    public UIManager(GameState gameState,
                     ClickHandler clickHandler,
                     UpgradeShop shop,
                     Inventory inventory,
                     SaveManager saveManager,
                     PassiveIncomeManager passiveIncomeManager,
                     SkinDropManager skinDropManager,
                     File saveFile) {
        this.gameState = gameState;
        this.clickHandler = clickHandler;
        this.shop = shop;
        this.inventory = inventory;
        this.saveManager = saveManager;
        this.passiveIncomeManager = passiveIncomeManager;
        this.skinDropManager = skinDropManager;
        this.saveFile = saveFile;
    }

    public void createAndShowUI() {
        JFrame frame = new JFrame("Energy Clicker");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        resourceLabel = new JLabel();
        passiveLabel = new JLabel();
        dropTimerLabel = new JLabel("Drop in: 60s");
        statusLabel = new JLabel("Ready");
        JPanel top = new JPanel(new GridLayout(1, 4));
        top.add(resourceLabel);
        top.add(passiveLabel);
        top.add(dropTimerLabel);
        top.add(statusLabel);
        frame.add(top, BorderLayout.NORTH);

        JButton clickButton = new JButton("Click to gain energy");
        clickButton.addActionListener(e -> clickHandler.click());
        frame.add(clickButton, BorderLayout.CENTER);

        upgradeModel = new DefaultTableModel(new Object[]{"Name", "Category", "Owned", "Price", "ID"}, 0);
        JTable upgradeTable = new JTable(upgradeModel);
        JButton buyButton = new JButton("Buy selected upgrade");
        buyButton.addActionListener(e -> {
            int row = upgradeTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) upgradeTable.getValueAt(row, 4);
                boolean success = shop.purchase(id);
                statusLabel.setText(success ? "Upgrade purchased" : "Not enough resources");
                refreshUpgrades();
            }
        });
        JPanel shopPanel = new JPanel(new BorderLayout());
        shopPanel.add(new JScrollPane(upgradeTable), BorderLayout.CENTER);
        shopPanel.add(buyButton, BorderLayout.SOUTH);

        inventoryModel = new DefaultTableModel(new Object[]{"Name", "Rarity", "Icon", "ID"}, 0);
        JTable inventoryTable = new JTable(inventoryModel);
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, shopPanel, inventoryPanel);
        bottomSplit.setResizeWeight(0.5);
        frame.add(bottomSplit, BorderLayout.SOUTH);

        frame.setSize(900, 600);
        frame.setVisible(true);

        nextDropAt = System.currentTimeMillis() + 60_000;
        startUiTimers();
        refreshUpgrades();
        refreshInventory();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    saveManager.save(saveFile, SaveData.from(gameState, inventory, shop.getUpgrades()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                passiveIncomeManager.shutdown();
                skinDropManager.shutdown();
            }
        });
    }

    private void startUiTimers() {
        Timer uiTimer = new Timer(500, e -> {
            resourceLabel.setText("Energy: " + String.format("%.2f", gameState.getResources()));
            passiveLabel.setText("Income/s: " + String.format("%.2f", gameState.getPassiveIncome()));
            long remaining = Math.max(0, (nextDropAt - System.currentTimeMillis()) / 1000);
            dropTimerLabel.setText("Drop in: " + remaining + "s");
        });
        uiTimer.start();
    }

    private void refreshUpgrades() {
        upgradeModel.setRowCount(0);
        for (Upgrade upgrade : shop.getUpgrades()) {
            upgradeModel.addRow(new Object[]{upgrade.getName(), upgrade.getCategory(), upgrade.getQuantity(), upgrade.currentPrice(), upgrade.getId()});
        }
    }

    private void refreshInventory() {
        inventoryModel.setRowCount(0);
        for (Skin skin : inventory.getSkins()) {
            inventoryModel.addRow(new Object[]{skin.getName(), skin.getRarity(), skin.getIconPath(), skin.getId()});
        }
    }

    @Override
    public void onSkinDropped(Skin skin) {
        nextDropAt = System.currentTimeMillis() + 60_000;
        refreshInventory();
        statusLabel.setText("New skin: " + skin);
    }
}
