# Contributing to ExtraNPC

Thanks for your interest in contributing.

## Before you start

1. Open an issue first for large changes (API breaks, new systems, refactors).
2. Keep pull requests focused — one feature or fix per PR.
3. Match the existing code style in `src/main/java/me/themoo/extranpc`.

## Development

- **Java:** 21+
- **Server API:** Paper 1.21+ (player NPCs: 1.21.9+ / 26.1+ with Mannequin)
- **Build:** `./gradlew build` (Windows: `gradlew.bat build`)
- Output JAR: `build/libs/ExtraNPC-<version>.jar`

## Pull request checklist

- [ ] Builds successfully with Gradle
- [ ] No unused debug spam in console
- [ ] Messages use MiniMessage where applicable
- [ ] Update `CHANGELOG.md` under `[Unreleased]` or the next version section
- [ ] Do not commit secrets, server configs, or `build/`

## Code of conduct (simple)

Be respectful. No harassment, spam, or malicious code. Violations may lead to ignored PRs / bans from support.

## Support

- Discord: [@m_1z.4](https://discord.com)
- Server: [https://discord.com/invite/fJTSA6vnVQ](https://discord.com/invite/fJTSA6vnVQ)
