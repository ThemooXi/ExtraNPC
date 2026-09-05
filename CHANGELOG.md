# Changelog

All notable changes to **ExtraNPC** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-09-05

### Changed
- Player NPCs always use Mannequin (real player model) on Paper 1.21.9+ / 26.1+ — removed ArmorStand fallback
- Clearer error message when Mannequin is unavailable; mob NPCs still work on older Paper 1.21.x

### Fixed
- Plugin loads safely on all Paper 1.21+ servers via reflective Mannequin provider loading

---

## [1.0.2] - 2026-09-05

### Added
- Multi-version player NPC support: Mannequin on Paper 1.21.9+ / 26.1+
- `PlayerNpcProviderFactory` loads Mannequin backend reflectively so the plugin enables on all Paper 1.21+ servers

### Changed
- Player NPC engine is selected automatically at runtime based on server capabilities
- Console banner shows active player NPC engine

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
