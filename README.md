<div align="center">

<img src="assets/extranpc-banner.png" alt="ExtraNPC" width="100%">

# ExtraNPC

**Standalone Advanced NPC Framework for Paper**

`GUI` · `Native Player NPCs` · `Trade Shops` · `PlaceholderAPI`

[![Release](https://img.shields.io/github/v/release/ThemooXi/ExtraNPC?style=for-the-badge&color=00B4D8)](https://github.com/ThemooXi/ExtraNPC/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Paper](https://img.shields.io/badge/Paper-1.19.4--26.2-00A98F?style=for-the-badge)](https://papermc.io)
[![Discord](https://img.shields.io/badge/Discord-Extra%20Flux-5865F2?style=for-the-badge)](https://discord.com/invite/fJTSA6vnVQ)

</div>

---

## About

ExtraNPC is a standalone Paper plugin for creating and managing NPCs.  
Build player-model NPCs, animals, and mobs with a full in-game GUI — skins, click commands, holograms, particles, and a built-in trade shop.

---

## Features

- **Player NPCs** — native Paper Mannequin where available, skinned compatible fallback on older versions
- **Mob NPCs** — animals and mobs as frozen NPCs
- **Skins** — Mojang player name or texture URL
- **Click actions** — `player:` · `console:` · `message:`
- **Shop system** — trade editor + villager merchant UI
- **Extras** — holograms, particles, look-at-player, glow, cooldown, permissions
- **PlaceholderAPI** — optional (works without it)
- **Admin tools** — sneak-click edit · clickable help in chat

---

## Requirements

| Requirement | Details |
|:------------|:--------|
| Server | Paper (or compatible fork) `1.19.4` through `26.2` |
| Java | `17+` bytecode; use the Java version required by your server |
| Dependencies | None required |

The same jar supports the complete range. On servers that expose the
Mannequin API (including `26.1` and `26.2`), PLAYER NPCs use the native
player model. Earlier versions automatically use a skinned ArmorStand
fallback, while mob NPCs and the remaining features continue to work.

---

## Installation

1. Download **[ExtraNPC-1.0.1.jar](https://github.com/ThemooXi/ExtraNPC/releases/latest)**
2. Put the jar in your `plugins` folder
3. Restart the server
4. Run `/extranpc gui`

> After an upgrade, delete `plugins/ExtraNPC/messages.yml` once if help/about look outdated.

---

## Quick start

```text
1. /extranpc gui           → create an NPC
2. Set name / skin / cmds  → configure it
3. Open Shop               → add trades
4. Right-click the NPC     → use it in-world
```

---

## Commands

> Aliases: `/extranpc` · `/enpc` · `/npc`  
> Base permission: `extranpc.admin`

| Command | Description |
|:--------|:------------|
| `/extranpc gui` | Open main GUI |
| `/extranpc help` | Clickable help |
| `/extranpc about` | Credits & Discord |
| `/extranpc update` | Check for updates |
| `/extranpc create <id> [type]` | Create NPC |
| `/extranpc edit <id>` | Edit NPC |
| `/extranpc delete <id>` | Delete NPC |
| `/extranpc list` | List NPCs |
| `/extranpc move <id>` | Move NPC to you |
| `/extranpc select <id>` | Select NPC |
| `/extranpc here` | Move selected here |
| `/extranpc tp <id>` | Teleport to NPC |
| `/extranpc reload` | Reload plugin |

---

## Permissions

| Permission | Default | Description |
|:-----------|:-------:|:------------|
| `extranpc.admin` | op | Full management |
| `extranpc.use` | true | Interact with NPCs |
| `extranpc.bypass` | op | Bypass permission & cooldown |

---

## Files

```text
plugins/ExtraNpc/
├── config.yml       Settings & defaults
├── messages.yml     Chat messages (MiniMessage)
└── npcs.yml         Saved NPCs
```

---

## Build

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

Output: `build/libs/ExtraNPC-1.0.1.jar`

---

<div align="center">

## Support

<a href="https://discord.com/invite/fJTSA6vnVQ">
  <img src="https://github.com/user-attachments/assets/93477f0a-4350-4d56-a0d8-f38bb3bf0c21" alt="Extra Flux Discord" width="360">
</a>

| | |
|:--:|:--:|
| **Discord** | `@m_1z.4` |
| **Server** | [Extra Flux](https://discord.com/invite/fJTSA6vnVQ) |
| **Spigot** | [ExtraNPC Plugin](https://www.spigotmc.org/resources/extranpc-plugin.138244/) |
| **ExtraBan** | [GitHub](https://github.com/ThemooXi/ExtraBan_Plugin) · [Spigot](https://www.spigotmc.org/resources/extraban-plugin.138140/) |
| **In-game** | `/extranpc about` |

</div>

---

<div align="center">

## License

Copyright © 2026 **ThemoO**  
Released under the [MIT License](LICENSE)

| Document | Link |
|:--------:|:----:|
| Attribution | [NOTICE](NOTICE) |
| Changelog | [CHANGELOG](CHANGELOG.md) |
| Security | [SECURITY](SECURITY.md) |
| Contributing | [CONTRIBUTING](CONTRIBUTING.md) |

<br>

<sub>- ExtraNPC · v1.0.1 · ThemoO · @m_1z.4</sub>

</div>
