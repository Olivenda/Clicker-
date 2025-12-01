# Energy Clicker

A Steam-enabled, Cookie Clicker inspired Java game with Swing UI, AES-encrypted saves, Steamworks-ready trading hooks and timed cosmetic drops.

## Features
- Active clicking and passive income loops.
- Upgrade shop with escalating prices and multiple categories.
- Cosmetic skin drops every 60 seconds with rarity tiers.
- Inventory viewer and upgrade browser in the UI.
- AES-256 + HMAC protected save files (XML payload encrypted on disk).
- Steamworks Web API integration entry points for login validation, inventory sync and trade offers (no spoofing or hacks).
- Extensible OOP architecture (GameState, Upgrade, Inventory, SaveManager, SteamIntegration, UIManager, etc.).

## Running
Compile and launch with JDK 17+:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.clicker.GameApplication
```

## Save data
Encrypted save file: `savegame.secure` in the working directory. Encryption keys derive from a local passphrase; update `PASSWORD` in `SaveManager` for production.

## Steamworks
The `SteamIntegration` class uses the official Steamworks Web API endpoints via `java.net.http`. Provide your App ID, API key and auth tickets before calling its methods. No spoofing or bypasses are implemented.

## Extending
- Add more upgrades by registering them in `GameApplication#seedUpgrades`.
- Introduce prestige, statistics or sounds by adding new managers alongside `PassiveIncomeManager`.
- Swap the UI layer by reimplementing `UIManager` while keeping the underlying managers intact.

## License
Apache License 2.0 — see [LICENSE](LICENSE).
