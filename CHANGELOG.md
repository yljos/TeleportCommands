# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


### [v1.1.0]

#### Added
- Added a server-side translation system
- Added a Json Storage cleaner, which automatically cleans and updates any values
- Added a safety check with `/tpa[here]` that automatically chooses a nearby safe location
- Added a CHANGELOG.md
- Added warp system with `/setwarp`, `/warp`, `/delwarp`, and `/warps` commands

#### Changed
- Simplified tpa commands to teleport directly without requiring acceptance
- Improved command messages and colors
- Improved performance by using Fabric API events instead of Mixins
- Improved `/home` `Already there` detection

#### Removed
- Removed `/back` command and all related functionality
- Removed `/tpaaccept` and `/tpadeny` commands (tpa now teleports directly)
- Removed mixin dependencies
- Removed multi-language support (English only)
- Removed Sources and Javadoc files to improve build speed
- Removed pretty json printing (to save storage)

#### Breaking changes (non-backwards compatible)
- Replaced `Player_UUID` in the storage json to `UUID`
- Changed Death location coords in the storage json from `double` to `int`
- Changed Home coords in the storage json from `double` to `int`
- 

### [v1.0.5]

#### Added
- Added support for NeoForge

#### Changed
- Changed mappings to Mojang.
- Cleaned up commands code
- Changed build files to support multiple mod loader


### [v1.0.2]

#### Added
- Added project icon
- Added project description

#### Changed
- Changed the array for tpa to only save the player's uuid (not the whole entity lmao)
