# Changelog

All notable changes to **ExtraNPC** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-09-05

### Added
- Runtime compatibility for **Paper 1.21.x, 26.1, and 26.2+**
- ArmorStand player-NPC fallback when Paper Mannequin is missing (1.21.0–1.21.8)
- Safe Material / EntityType / Particle / Attribute lookups across renamed 1.21 and 26.x APIs
- ArmorStand hologram fallback when TextDisplay is unavailable
- Console banner now shows detected server version and player-NPC backend

### Changed
- Player NPCs pick Mannequin on 1.21.9+ / 26.1 / 26.2, otherwise ArmorStand
- Mob type GUI and tab-complete hide entity types the server does not have
- `plugin.yml` still uses `api-version: 1.21` so older 1.21 servers can load the same JAR

---

## [1.0.1] - 2026-08-25

### Added
- Discord support reminders for operators (welcome on join, optional repeat every 7 days)
- Update checker with Spigot lookup and Discord links in console / in-game messages
- `/extranpc update` command for manual update checks
- `support-reminder.yml` to track reminder state per operator

### Changed
- Clearer console banner with Discord support line
- Enhanced `/extranpc help` and `/extranpc about` with Discord call-to-action

---

## [1.0.0] - 2026-08-24

### Added
- First public release of ExtraNPC
- Standalone NPC framework for Paper (no Citizens / Shopkeepers required)
- Native player NPCs via Paper Mannequin (real player model)
- Animal / mob NPC types
- Skins by Mojang player name or texture URL
- Full in-game GUI (create, edit, list, settings, skins, particles, commands)
- Click actions: `player:`, `console:`, `message:`
- Built-in shop trade editor and villager merchant UI
- Holograms, particles, look-at-player, glow, baby mode, cooldown, permissions
- Optional PlaceholderAPI support
- Clickable help / about messages in chat
- Console ASCII banner with credits

### Support
- Discord: **@m_1z.4**
- Server: [Extra Flux](https://discord.com/invite/fJTSA6vnVQ)
