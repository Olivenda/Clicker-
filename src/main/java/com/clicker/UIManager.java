package com.clicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Builds the Swing UI and wires user interactions to the core game systems.
 */
public class UIManager implements SkinDropManager.DropListener {
    private static final long DROP_INTERVAL_MS = 60_000L;

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
    private JLabel clickValueLabel;
    private JLabel autoClickLabel;
    private JLabel dropTimerLabel;
    private JLabel statusLabel;
    private JProgressBar dropProgressBar;
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
        frame.getContentPane().setBackground(new Color(13, 19, 29));

        Font titleFont = new Font("SansSerif", Font.BOLD, 16);
        Font valueFont = new Font("SansSerif", Font.BOLD, 20);

        resourceLabel = new JLabel();
        passiveLabel = new JLabel();
        clickValueLabel = new JLabel();
        autoClickLabel = new JLabel();
        dropTimerLabel = new JLabel("Next drop in: 60s");
        statusLabel = new JLabel("Ready");

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        statsPanel.add(createStatCard("⚡ Energy", resourceLabel, new Color(111, 202, 255), titleFont, valueFont));
        statsPanel.add(createStatCard("💤 Passive", passiveLabel, new Color(165, 130, 255), titleFont, valueFont));
        statsPanel.add(createStatCard("🖱️ Per Click", clickValueLabel, new Color(255, 186, 108), titleFont, valueFont));
        statsPanel.add(createStatCard("🤖 Auto", autoClickLabel, new Color(118, 255, 206), titleFont, valueFont));
        frame.add(statsPanel, BorderLayout.NORTH);

        JButton clickButton = buildClickButton();
        JButton dollarButton = buildDollarButton();

        JPanel actionPanel = new GradientPanel();
        actionPanel.setLayout(new BorderLayout());
        actionPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        actionPanel.add(buildActionButtonRow(clickButton, dollarButton), BorderLayout.CENTER);
        actionPanel.add(buildDropPanel(), BorderLayout.SOUTH);
        frame.add(actionPanel, BorderLayout.CENTER);

        JTable upgradeTable = buildUpgradeTable();
        JTable inventoryTable = buildInventoryTable();

        JPanel shopPanel = new JPanel(new BorderLayout());
        shopPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        shopPanel.setBackground(new Color(22, 30, 43));
        JLabel shopTitle = sectionTitle("Upgrades");
        shopPanel.add(shopTitle, BorderLayout.NORTH);
        shopPanel.add(new JScrollPane(upgradeTable), BorderLayout.CENTER);
        JButton buyButton = new JButton("Buy selected upgrade");
        buyButton.setBackground(new Color(60, 160, 255));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);
        buyButton.addActionListener(e -> purchaseSelectedUpgrade(upgradeTable));
        shopPanel.add(buyButton, BorderLayout.SOUTH);

        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        inventoryPanel.setBackground(new Color(22, 30, 43));
        JLabel inventoryTitle = sectionTitle("Skins");
        inventoryPanel.add(inventoryTitle, BorderLayout.NORTH);
        inventoryPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, shopPanel, inventoryPanel);
        bottomSplit.setResizeWeight(0.5);
        bottomSplit.setBorder(new EmptyBorder(8, 8, 8, 8));
        frame.add(bottomSplit, BorderLayout.SOUTH);

        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        nextDropAt = System.currentTimeMillis() + DROP_INTERVAL_MS;
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

    private JButton buildClickButton() {
        JButton clickButton = new JButton("Charge Energy");
        clickButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        clickButton.setPreferredSize(new Dimension(300, 200));
        clickButton.setBackground(new Color(76, 170, 255));
        clickButton.setForeground(Color.WHITE);
        clickButton.setFocusPainted(false);
        clickButton.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        clickButton.setToolTipText("Press SPACE or ENTER to rapidly click");
        clickButton.addActionListener(e -> performClick());

        InputMap inputMap = clickButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "do-click");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "do-click");
        clickButton.getActionMap().put("do-click", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                performClick();
            }
        });

        return clickButton;
    }

    private JButton buildDollarButton() {
        JButton dollarButton = new JButton("Get $1");
        dollarButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        dollarButton.setPreferredSize(new Dimension(300, 200));
        dollarButton.setBackground(new Color(76, 193, 96));
        dollarButton.setForeground(Color.WHITE);
        dollarButton.setFocusPainted(false);
        dollarButton.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        dollarButton.setToolTipText("Click to instantly earn $1");
        dollarButton.addActionListener(e -> addOneDollar());
        return dollarButton;
    }

    private JPanel buildActionButtonRow(JButton clickButton, JButton dollarButton) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.add(clickButton);
        panel.add(dollarButton);
        return panel;
    }

    private JPanel buildDropPanel() {
        JPanel dropPanel = new JPanel(new BorderLayout(12, 4));
        dropPanel.setOpaque(false);
        dropPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        dropProgressBar = new JProgressBar(0, (int) DROP_INTERVAL_MS);
        dropProgressBar.setStringPainted(true);
        dropProgressBar.setForeground(new Color(255, 186, 108));
        dropProgressBar.setBackground(new Color(32, 43, 60));
        dropProgressBar.setBorder(BorderFactory.createLineBorder(new Color(255, 186, 108), 1));

        statusLabel.setForeground(Color.LIGHT_GRAY);
        dropPanel.add(dropTimerLabel, BorderLayout.WEST);
        dropPanel.add(dropProgressBar, BorderLayout.CENTER);
        dropPanel.add(statusLabel, BorderLayout.EAST);
        return dropPanel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(0, 4, 8, 4));
        return label;
    }

    private JTable buildUpgradeTable() {
        upgradeModel = new DefaultTableModel(new Object[]{"Upgrade", "Effect", "Owned", "Next Price", "Category", "ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(upgradeModel);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(28, 38, 53));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(46, 58, 76));
        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setWidth(0);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    purchaseSelectedUpgrade(table);
                }
            }
        });
        return table;
    }

    private JTable buildInventoryTable() {
        inventoryModel = new DefaultTableModel(new Object[]{"Name", "Rarity", "Icon", "ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(inventoryModel);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(new Color(28, 38, 53));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(46, 58, 76));
        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setWidth(0);
        return table;
    }

    private void purchaseSelectedUpgrade(JTable upgradeTable) {
        int row = upgradeTable.getSelectedRow();
        if (row >= 0) {
            String id = (String) upgradeTable.getValueAt(row, 5);
            boolean success = shop.purchase(id);
            setStatus(success ? "Upgrade purchased" : "Not enough energy", success ? new Color(118, 255, 206) : new Color(255, 120, 120));
            refreshUpgrades();
        }
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, Font titleFont, Font valueFont) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(24, 32, 45));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(accent);
        valueLabel.setFont(valueFont);
        valueLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private void startUiTimers() {
        Timer uiTimer = new Timer(400, e -> {
            updateStatsLabels();
            long remainingMs = Math.max(0, nextDropAt - System.currentTimeMillis());
            dropTimerLabel.setText("Next drop in: " + (remainingMs / 1000) + "s");
            dropProgressBar.setMaximum((int) DROP_INTERVAL_MS);
            dropProgressBar.setValue((int) (DROP_INTERVAL_MS - remainingMs));
            dropProgressBar.setString((remainingMs / 1000) + "s");
        });
        uiTimer.start();
    }

    private void updateStatsLabels() {
        resourceLabel.setText(String.format("%s", formatDouble(gameState.getResources())));
        passiveLabel.setText(String.format("%s /s", formatDouble(gameState.getPassiveIncome())));
        clickValueLabel.setText(String.format("%s /click", formatDouble(gameState.getClickValue())));
        autoClickLabel.setText(String.format("%s /s", formatDouble(gameState.getAutoClickPerSecond())));
    }

    private String formatDouble(double value) {
        if (value >= 1_000_000) {
            return String.format("%.2fM", value / 1_000_000);
        }
        if (value >= 1_000) {
            return String.format("%.1fk", value / 1_000);
        }
        return String.format("%.2f", value);
    }

    private void refreshUpgrades() {
        upgradeModel.setRowCount(0);
        for (Upgrade upgrade : shop.getUpgrades()) {
            upgradeModel.addRow(new Object[]{
                    upgrade.getName(),
                    describeEffect(upgrade),
                    upgrade.getQuantity(),
                    upgrade.currentPrice(),
                    upgrade.getCategory(),
                    upgrade.getId()
            });
        }
    }

    private String describeEffect(Upgrade upgrade) {
        return switch (upgrade.getEffectType()) {
            case CLICK_VALUE -> String.format("+%s / click", formatDouble(upgrade.getEffectPerPurchase()));
            case PASSIVE_INCOME -> String.format("+%s / s", formatDouble(upgrade.getEffectPerPurchase()));
            case AUTO_CLICK -> String.format("+%s auto / s", formatDouble(upgrade.getEffectPerPurchase()));
            default -> upgrade.getDescription();
        };
    }

    private void refreshInventory() {
        inventoryModel.setRowCount(0);
        for (Skin skin : inventory.getSkins()) {
            inventoryModel.addRow(new Object[]{skin.getName(), skin.getRarity(), skin.getIconPath(), skin.getId()});
        }
    }

    private void setStatus(String text, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
    }

    private void performClick() {
        clickHandler.click();
        setStatus("Energy charged!", new Color(118, 255, 206));
    }

    private void addOneDollar() {
        gameState.addResources(1);
        setStatus("$1 collected!", new Color(118, 255, 206));
    }

    @Override
    public void onSkinDropped(Skin skin) {
        nextDropAt = System.currentTimeMillis() + DROP_INTERVAL_MS;
        refreshInventory();
        setStatus("New skin: " + skin, new Color(111, 202, 255));
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(0, 0, new Color(18, 26, 39), 0, getHeight(), new Color(10, 14, 22));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
