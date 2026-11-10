# 🎁 SimpleCrates

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-Bedrock%20Edition-00AA00?style=for-the-badge&logo=minecraft&logoColor=white" />
  <img src="https://img.shields.io/badge/Server-NukkitPetteriM1Edition-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Language-Java%2025-blue?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
</p>

<p align="center">
  **An enhanced crate system for Minecraft Bedrock Edition servers running NukkitPetteriM1Edition.**
</p>

---

## 📖 About

**SimpleCrates** is a fork of the original [SimpleCrates v1.0.2](https://cloudburstmc.org/resources/simplecrates.145/), originally developed by **Ceremonious**.

This project preserves the original crate system while introducing new features, visual effects, inventory interfaces and opening animations, improving the overall experience for Minecraft Bedrock Edition players.

The plugin is developed and adapted for **NukkitPetteriM1Edition**.

## ✨ Features

### 🎁 Original Features
- Create and manage custom crates.
- Physical and virtual crate keys.
- Configurable rewards with weighted chances.
- Item rewards with custom names, lore and enchantments.
- Console commands as rewards.
- Player commands as rewards.
- Configurable reward rounds.
- Custom key names and lore.
- Preview crate rewards.

### 🆕 Added Features

**🎨 Particle Effects**
- Configurable particles around crates.
- Available particle types: FLAME, SMOKE and HEART.
- Adjustable particle interval, radius and number of points.
- Enable or disable effects through `effects.yml`.

**🔍 Reward Preview**
- Inventory interface powered by FakeInventories.
- Displays reward names, lore and quantities.
- Shows the probability assigned to each reward.

**🔊 Crate Effects**
- Knockback and sound effects when a player attempts to open a crate without a key.
- Configurable opening sounds.
- Sound effects during roulette movement.
- Configurable final roulette sound.

**🎬 Opening Animations**

Two animation modes are available:

- **Classic:** Opening animation with configurable particles and sounds.
- **Roulette:** Animated reward selection inside a virtual inventory.

The roulette includes:
- Moving reward items.
- Progressive animation slowdown.
- Highlighted winning reward in the center.
- Gold blocks decorating the winning reward.
- Configurable spin and finish sounds.
- Configurable animation duration.

## 📦 Dependencies

- [NukkitPetteriM1Edition](https://github.com/PetteriM1/NukkitPetteriM1Edition/releases/tag/4567)
- [FakeInventories](https://github.com/PetteriM1/FakeInventories-1)

**Important:** FakeInventories must be installed on your server for the inventory interfaces and roulette animation to work.

## ⚙️ Installation

1. Download or compile the SimpleCrates JAR.
2. Download and install FakeInventories.
3. Place both JAR files inside your server's `plugins` folder.
4. Start or restart your server.
5. Configure your crates and their rewards.

## 📝 Configuration

SimpleCrates generates configuration files in its plugin folder.

| File | Description |
|---|---|
| `config.yml` | Main configuration, crate settings, keys and rewards. |
| `effects.yml` | Particle effects around crates. |
| `animations.yml` | Opening animation mode, particles, duration and sounds. |

### 🎨 effects.yml

Configure the particles displayed around your crates.

Available particle types:
- `FLAME`
- `SMOKE`
- `HEART`

You can enable or disable particles and customize their interval, radius and distribution points.

### 🎬 animations.yml

Configure the crate opening animations.

Available modes:
- `classic`
- `roulette`

Each animation has its own settings for duration, particles and sounds.

You can also enable or disable the sound effects used during the opening process.

## 🎮 Commands

| Command | Description |
|---|---|
| `/keys` | Check your virtual crate keys. |
| `/crates read` | Display information about the item in your hand. |
| `/crates list` | List existing crates. |
| `/crates create <crate>` | Create a crate. |
| `/crates delete <crate>` | Delete a crate. |
| `/crates setblock <crate>` | Set the crate block. |
| `/crates additem <crate>` | Add an item reward. |
| `/crates addcmd <crate> <command>` | Add a console reward command. |
| `/crates addplayercmd <crate> <command>` | Add a player reward command. |
| `/crates givekey <player> <crate> <amount>` | Give crate keys to a player. |

### 🔐 Permissions

| Permission | Description |
|---|---|
| `crates.admin` | Access to crate administration commands. |
| `crates.keys` | Access to the `/keys` command. |

## ⚖️ Reward Chances

The reward chance system uses **weights**, not fixed percentages.

For example:

| Reward | Weight |
|---|---:|
| Diamond | 50 |
| Emerald | 30 |
| Netherite Ingot | 20 |
| **Total** | **100** |

The probability of obtaining a reward is calculated using:

`(Reward Weight / Total Weight) × 100`

The total weight does not have to equal 100.

## 🤝 Credits

- **Ceremonious:** Original SimpleCrates plugin and its original functionality.
- **PetteriM1:** NukkitPetteriM1Edition.
- **IWareQ / PetteriM1:** FakeInventories.

This project is an independent fork and is not an official release of the original SimpleCrates author.

## 🐛 Bug Reports & Support

If you encounter a bug or have a suggestion, please report it through:

- [GitHub Issues](https://github.com/BrunoJaegerK/SimpleCrates1/issues)
- [GitHub Discussions](https://github.com/BrunoJaegerK/SimpleCrates1/discussions)

Please include the error message, server version and steps to reproduce the problem.

---

<p align="center">
  **Made with ❤️ for Minecraft Bedrock Edition**
</p>

<p align="center">
  This plugin was created for DELUXEMINE NETWORK and its development depends entirely on your support.
</p>
