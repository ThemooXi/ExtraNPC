# Changelog

All notable changes to **ExtraNPC** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- One-jar compatibility for Paper 1.19.4 through 26.2, including 26.1
- Automatic skinned ArmorStand fallback for PLAYER NPCs on servers without the Mannequin API

### Changed
- Load Mannequin and version-specific entity/material constants safely at runtime
- Emit Java 17-compatible bytecode while retaining modern-server support

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
