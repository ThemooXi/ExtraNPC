# ExtraNPC

<p align="center">
  <img src="assets/extranpc-banner.png" alt="ExtraNPC" width="100%">
</p>

<p align="center">
  <strong>Standalone Advanced NPC Framework for Paper</strong><br>
  GUI · Native Player NPCs · Trade Shops · Optional PlaceholderAPI
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License"></a>
  <a href="CHANGELOG.md"><img src="https://img.shields.io/badge/version-1.0.0-blue.svg" alt="Version"></a>
  <a href="https://discord.com/invite/fJTSA6vnVQ"><img src="https://img.shields.io/badge/Discord-Extra%20Flux-5865F2.svg" alt="Discord"></a>
</p>

```
  _____      _              _   _ ____   ____ 
 | ____|_  _| |_ _ __ __ _ | \ | |  _ \ / ___|
 |  _| \ \/ / __| '__/ _` ||  \| | |_) | |    
 | |___ >  <| |_| | | (_| || |\  |  __/| |___ 
 |_____/_/\_\\__|_|  \__,_||_| \_|_|    \____|
```

---

## Credits / الحقوق

| | |
|---|---|
| **Author** | ThemoO |
| **Discord** | `@m_1z.4` |
| **Support Server** | [Extra Flux](https://discord.com/invite/fJTSA6vnVQ) |
| **License** | [MIT](LICENSE) |

---

## Overview

**ExtraNPC** is a **standalone** Paper plugin.  
No Citizens. No Shopkeepers. No required soft-dependencies.

You get:

- Native **player NPCs** (Paper Mannequin — real player model)
- Animals / mobs as frozen NPCs
- Skins by **player name** or **texture URL**
- Click commands (player / console / message)
- Built-in **shop trade editor** + villager merchant UI
- Holograms, particles, look-at-player, glow, cooldown, permissions
- Optional **PlaceholderAPI** (works without it)
- Clickable commands in chat help

---

## Requirements

| Requirement | Details |
|-------------|---------|
| Server | **Paper** (or fork) **1.21+** |
| Java | **21+** |
| Other plugins | **None required** |

Optional: PlaceholderAPI for placeholders in commands/messages.

---

## Installation

1. Download `ExtraNPC-1.0.0.jar` from [Releases](../../releases)
2. Put it in `plugins/`
3. Restart the server
4. Check console for the ExtraNPC ASCII banner
5. In-game: `/extranpc gui`

> Upgrading messages: delete `plugins/ExtraNPC/messages.yml` once if help/about look outdated.

---

## Build from source

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

Output: `build/libs/ExtraNPC-1.0.0.jar`

---

## Commands

Aliases: `/extranpc` · `/enpc` · `/npc`  
Permission: `extranpc.admin`

| Command | Description |
|---------|-------------|
| `/extranpc` / `/extranpc gui` | Main GUI |
| `/extranpc help` | Help (clickable) |
| `/extranpc about` | Credits & Discord |
| `/extranpc create <id> [type]` | Create NPC |
| `/extranpc edit <id>` | Edit GUI |
| `/extranpc delete <id>` | Delete |
| `/extranpc list` | List (click id to edit) |
| `/extranpc move <id>` | Move NPC to you |
| `/extranpc select <id>` | Select NPC |
| `/extranpc here` | Move selected |
| `/extranpc tp <id>` | Teleport to NPC |
| `/extranpc reload` | Reload |

---

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `extranpc.admin` | op | Full management |
| `extranpc.use` | true | Interact with NPCs |
| `extranpc.bypass` | op | Bypass permission + cooldown |

---

## Features

### NPC types
- `PLAYER` — real player model (Paper Mannequin, no ArmorStand / no Citizens)
- Animals & mobs: cow, pig, villager, zombie, allay, sniffer, armadillo, …

### Skins
- Mojang player name
- Texture URL
- Clear skin in GUI

### Interaction
- Left / right click commands
- `player:` / `console:` / `message:`
- `{player}`, `%player_name%`, PlaceholderAPI (optional)

### Shop
- Recipe rows: `Cost1 + Cost2 → Result`
- Admin (infinite) or limited uses
- Auto preview after setup/edit
- Opens as merchant UI for players

### Extra
- Look at players, holograms, particles
- Invulnerable / gravity / silent / glowing / baby
- Cooldown + interact permission
- Admin sneak-click to edit

---

## Quick start

1. `/extranpc gui` → Create NPC
2. Set name / skin / commands
3. Open **Shop** → place trades → close for preview
4. Right-click NPC to use

---

## Files

```text
plugins/ExtraNpc/
  config.yml
  messages.yml
  npcs.yml
```

---

## License & rights

- Copyright © 2026 **ThemoO**
- Released under the **[MIT License](LICENSE)**
- See **[NOTICE](NOTICE)** for attribution and third-party notes
- Changes are tracked in **[CHANGELOG.md](CHANGELOG.md)**
- Security reports: see **[SECURITY.md](SECURITY.md)**
- Contributions: see **[CONTRIBUTING.md](CONTRIBUTING.md)**

---

<img width="448" height="64" alt="badge" src="https://github.com/user-attachments/assets/93477f0a-4350-4d56-a0d8-f38bb3bf0c21" />


## Support

- Discord: **@m_1z.4**
- Server: **[https://discord.com/invite/fJTSA6vnVQ](https://discord.com/invite/fJTSA6vnVQ)**
- In-game: `/extranpc about`

---

<p align="center">
  <sub>ExtraNPC · v1.0.0 · Standalone · ThemoO · @m_1z.4</sub>
</p>
