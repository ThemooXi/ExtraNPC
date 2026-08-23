# ExtraNPC

<p align="center">
  <img src="assets/extranpc-banner.png" alt="ExtraNPC banner" width="100%">
</p>

<p align="center">
  <b>Standalone Advanced NPC Framework for Paper</b><br>
  <sub>GUI · Native Player NPCs · Trade Shops · Optional PlaceholderAPI</sub>
</p>

<p align="center">
  <a href="https://github.com/ThemooXi/ExtraNPC/releases"><img src="https://img.shields.io/badge/version-1.0.0-blue.svg" alt="Version"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License"></a>
  <a href="https://discord.com/invite/fJTSA6vnVQ"><img src="https://img.shields.io/badge/Discord-Extra%20Flux-5865F2.svg" alt="Discord"></a>
  <a href="https://papermc.io"><img src="https://img.shields.io/badge/Paper-1.21%2B-00A98F.svg" alt="Paper"></a>
</p>

---

## Table of contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Commands](#commands)
- [Permissions](#permissions)
- [Config files](#config-files)
- [Build from source](#build-from-source)
- [Support](#support)
- [License](#license)

---

## Overview

**ExtraNPC** is a standalone Paper plugin — no Citizens, no Shopkeepers, no required soft-dependencies.

Create player-model NPCs, animals, and mobs with a full in-game GUI, click actions, skins, holograms, particles, and a built-in trade shop.

| | |
|---|---|
| **Author** | ThemoO |
| **Discord** | `@m_1z.4` |
| **Support** | [Extra Flux](https://discord.com/invite/fJTSA6vnVQ) |
| **License** | [MIT](LICENSE) |

---

## Features

| Area | What you get |
|------|----------------|
| **NPC types** | `PLAYER` (Paper Mannequin), animals & mobs |
| **Skins** | Mojang name, texture URL, clear in GUI |
| **Actions** | Left / right click · `player:` / `console:` / `message:` |
| **Placeholders** | `{player}`, `%player_name%`, optional PlaceholderAPI |
| **Shop** | Trade editor · villager merchant UI · infinite or limited uses |
| **Extras** | Holograms, particles, look-at-player, glow, baby, cooldown, permissions |
| **Admin** | Sneak-click to edit · clickable help / about in chat |

---

## Requirements

| Requirement | Details |
|-------------|---------|
| Server | Paper (or fork) **1.21+** |
| Java | **21+** |
| Dependencies | **None** (PlaceholderAPI optional) |

---

## Installation

1. Download [`ExtraNPC-1.0.0.jar`](https://github.com/ThemooXi/ExtraNPC/releases/latest) from Releases
2. Place it in `plugins/`
3. Restart the server
4. Run `/extranpc gui`

> After upgrading, delete `plugins/ExtraNPC/messages.yml` once if help/about look outdated.

---

## Quick start

1. `/extranpc gui` → create an NPC  
2. Set name, skin, and commands  
3. Open **Shop** → add trades → close for preview  
4. Right-click the NPC in-world to use it  

---

## Commands

**Aliases:** `/extranpc` · `/enpc` · `/npc`  
**Permission:** `extranpc.admin`

| Command | Description |
|---------|-------------|
| `/extranpc` · `/extranpc gui` | Open main GUI |
| `/extranpc help` | Clickable help |
| `/extranpc about` | Credits & Discord |
| `/extranpc create <id> [type]` | Create NPC |
| `/extranpc edit <id>` | Open edit GUI |
| `/extranpc delete <id>` | Delete NPC |
| `/extranpc list` | List NPCs (click id to edit) |
| `/extranpc move <id>` | Move NPC to you |
| `/extranpc select <id>` | Select NPC |
| `/extranpc here` | Move selected NPC here |
| `/extranpc tp <id>` | Teleport to NPC |
| `/extranpc reload` | Reload config & data |

---

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `extranpc.admin` | op | Full management |
| `extranpc.use` | true | Interact with NPCs |
| `extranpc.bypass` | op | Bypass interact permission & cooldown |

---

## Config files

```text
plugins/ExtraNpc/
├── config.yml      # Settings & defaults
├── messages.yml    # Chat messages (MiniMessage)
└── npcs.yml        # Saved NPCs
```

---

## Build from source

```bash
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

Output: `build/libs/ExtraNPC-1.0.0.jar`

---

<p align="center">

## Support

<a href="https://discord.com/invite/fJTSA6vnVQ">
  <img width="448" height="64" alt="Join Extra Flux Discord" src="https://github.com/user-attachments/assets/93477f0a-4350-4d56-a0d8-f38bb3bf0c21">
</a>

<br><br>

**Discord** · `@m_1z.4`<br>
**Server** · [Extra Flux](https://discord.com/invite/fJTSA6vnVQ)<br>
**In-game** · `/extranpc about`

</p>

---

<p align="center">

## License

Copyright © 2026 **ThemoO**<br>
Released under the [MIT License](LICENSE)

<br>

[NOTICE](NOTICE) ·
[CHANGELOG](CHANGELOG.md) ·
[SECURITY](SECURITY.md) ·
[CONTRIBUTING](CONTRIBUTING.md)

</p>

---

<p align="center">
  <sub>ExtraNPC · v1.0.0 · ThemoO · @m_1z.4</sub>
</p>
