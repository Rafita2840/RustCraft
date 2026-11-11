# RustCraft

A Fabric mod for Minecraft 1.21.1 that brings the main features of Rust into Minecraft.

See `docs/PLAN.md` for the roadmap and `docs/STATUS.md` for what works today.

## Development

```
./gradlew build         # compile and package
./gradlew runClient     # start a client
./gradlew runServer     # start a dedicated server (accept the EULA in run/eula.txt)
./gradlew runDatagen    # regenerate src/main/generated
```

### Adding a block or item

1. Register it in `ModBlocks` / `ModItems` and add it to the creative tab in `ModGroups`.
2. Add it to the datagen providers in `net.rafa.rustcraft.datagen`: model, lang name, loot table, recipe, tags.
3. Run `./gradlew runDatagen`.
4. Commit the changes in `src/main/generated` together with your code.

Do not write JSON for models, blockstates, lang, loot tables, recipes or tags by hand. The only hand-made
assets are the Blockbench models, textures, sounds and `fabric.mod.json`.
CI re-runs datagen and fails if `src/main/generated` is out of date.

### Client and server code

Client-only code lives in `src/client/java`. `src/main` must never import `net.minecraft.client`.
All game logic runs on the server.
