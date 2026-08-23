# ExtraNPC

<p align="center">
  <img src="assets/extranpc-banner.png" alt="ExtraNPC" width="900">
</p>

<p align="center">
  <b>Standalone Advanced NPC Framework for Paper</b><br>
  GUI · Native Player NPCs · Trade Shops · Optional PlaceholderAPI
</p>

<p align="center">
  <a href="https://github.com/ThemooXi/ExtraNPC/releases/latest"><img src="https://img.shields.io/github/v/release/ThemooXi/ExtraNPC?style=flat-square&color=00B4D8" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="License"></a>
  <a href="https://papermc.io"><img src="https://img.shields.io/badge/Paper-1.21%2B-00A98F?style=flat-square" alt="Paper"></a>
  <a href="https://discord.com/invite/fJTSA6vnVQ"><img src="https://img.shields.io/badge/Discord-Extra%20Flux-5865F2?style=flat-square" alt="Discord"></a>
</p>

---

## About

ExtraNPC is a **standalone** Paper plugin.  
No Citizens. No Shopkeepers. No required soft-dependencies.

Create player-model NPCs, animals, and mobs with a full in-game GUI — skins, click commands, holograms, particles, and a built-in trade shop.

---

## Features

- **Player NPCs** — real player model via Paper Mannequin
- **Mob NPCs** — animals and mobs as frozen NPCs
- **Skins** — Mojang player name or texture URL
- **Click actions** — `player:` · `console:` · `message:`
- **Shop system** — trade editor + villager merchant UI
- **Extras** — holograms, particles, look-at-player, glow, cooldown, permissions
- **PlaceholderAPI** — optional (works without it)
- **Admin tools** — sneak-click edit · clickable help in chat

---

## Requirements

| | |
|:--|:--|
| **Server** | Paper (or fork) `1.21+` |
| **Java** | `21+` |
| **Dependencies** | None required |

---

## Installation

1. Download **[ExtraNPC-1.0.0.jar](https://github.com/ThemooXi/ExtraNPC/releases/latest)**
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
