# RustCraft

A Fabric mod for Minecraft 1.21.1 that brings the main features of Rust into Minecraft.

See `docs/PLAN.md` for the roadmap and `docs/STATUS.md` for what works today.

## Development

```
./gradlew build         # compile and package
./gradlew runClient     # start a client
./gradlew runServer     # start a dedicated server (accept the EULA in run/eula.txt)
./gradlew runDatagen    # regenerate src/main/generated
./gradlew runGametest   # run the game tests in src/gametest
```

CI runs the build, the game tests, a datagen up-to-date check and a dedicated server boot on every push.

### Adding a block or item

1. Register it in `ModItems` / `ModBlocks` with its `ModCategory`. The creative tabs fill themselves from the categories.
2. Add it to the datagen providers in `net.rafa.rustcraft.datagen`: model, lang name, loot table, recipe, tags.
3. Run `./gradlew runDatagen`.
4. Commit the changes in `src/main/generated` together with your code.

Do not write JSON for models, blockstates, lang, loot tables, recipes or tags by hand. The only hand-made
assets are the Blockbench models, textures, sounds, `fabric.mod.json` and the mixin config.
CI re-runs datagen and fails if `src/main/generated` is out of date.

### Client and server code

Client-only code lives in `src/client/java`. `src/main` must never import `net.minecraft.client`.
All game logic runs on the server.

### Player stats

Per-player values (thirst, and later radiation, bleeding…) live in `data/RustPlayerData`, saved through a Fabric
data attachment. Change them only through `data/PlayerStats`: it syncs the client when a displayed value changes.

### Config, game rules and debug commands

- `config/rustcraft.json` (in the run folder): balance values such as thirst drain. Created on first start.
- `/gamerule rustcraftThirstEnabled true|false`: per-world switch.
- `/rustcraft reload`: re-read the config without restarting.
- `/rustcraft stats get <player>` and `/rustcraft stats set thirst <0-20> [player]`.

All `/rustcraft` commands need operator permission.

### Tests

Game tests live in `src/gametest` (a separate test mod that is never part of the jar). Keep game rules in plain
functions (like `data/ThirstMath`) so they can be tested without a player or world.
