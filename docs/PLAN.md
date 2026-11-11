# RustCraft – Development Plan

> This plan covers how to bring the main features of **Rust** (Facepunch) into **Minecraft** as a **Fabric** mod.
> It is built on `docs/ToDo.txt`. Every item from that list is kept, and some are grouped, split or expanded. The traceability table at the end maps each ToDo line to the tasks that cover it.

---

## 0. How to read this plan

### 0.1 Project facts (from the repo, Nov 2026)

| Thing | Value |
|---|---|
| Loader | Fabric Loader `0.16.14` (`fabric.mod.json` → `>=0.16.14`) |
| Minecraft | `1.21` (`~1.21`), Yarn `1.21+build.9` |
| Fabric API | `0.102.0+1.21` |
| Loom | `1.11-SNAPSHOT` |
| Java | 21 |
| Mod id / package | `rustcraft` / `net.rafa.rustcraft` |
| Repo | https://github.com/Rafita2840/RustCraft (branch `master`, CI = `.github/workflows/build.yml`) |
| Datagen | Entrypoint `RustCraftDataGenerator` exists but is **empty**. `fabricApi { configureDataGeneration { client = true } }` is already in `build.gradle`. |

### 0.2 Task format

Each task has:

- **ID**: `P<phase>.<number>` (e.g. `P0.3`). Reference it in commit messages and branch names (`feat/P2.4-hemp`).
- **Goal**: one line saying what the task is for.
- **Description / Steps**: what to actually do.
- **Done when**: acceptance criteria. Don't tick the task off until all of them hold.
- **Depends on**: tasks that must land first.
- **Size**: `S` ≈ an evening, `M` ≈ a weekend, `L` ≈ a week or more, `XL` = several weeks, should be split again when you start it.

### 0.3 General rules for the whole project

1. **Datagen first.** After Phase 0, no new hand-written JSON for models, blockstates, lang, loot tables, recipes or tags. The exception is complex Blockbench models (vending machine, crate, guns, bear…), which stay as hand-made model JSON in `src/main/resources`. Datagen still produces their blockstates, lang and loot.
2. **Server-authoritative.** All game logic (thirst, damage, trades, building, shooting) runs on the server. The client only renders and sends input packets. The mod must also run on a **dedicated server**, so test that regularly (see P0.3).
3. **Data-driven where cheap.** Balance numbers (gather rates, building costs, gun stats, loot, trades, upkeep) should live in JSON or a config so you can tune them without recompiling.
4. **One feature = one package.** For example `net.rafa.rustcraft.thirst`, `.building`, `.gun`, `.loot`, `.effect`. Each package registers itself from one `init()` method.
5. **Keep vanilla playable.** RustCraft adds to Minecraft. Disabling vanilla systems (vanilla crafting, natural regen, vanilla ores…) should be a config or gamerule option, not hard-coded.
6. **Small PRs.** One task (or sub-task) per branch/PR so CI can catch breakages early.

---

## 1. Current state (snapshot from reviewing the code)

This is what already exists. P0.1 is to confirm and finish this audit, but most of it is already written down here.

### 1.1 What exists

| Area | Content |
|---|---|
| **Items** | `scrap`, `low_grade_fuel`, `cloth`, `sulfur`, `animal_fat`, `stone`, `wood`, `metal`, `high_quality_metal`, `hammer`, `blueprint_floor`, `blueprint_wall`, `blueprint_stairs`, hazmat suit (4 armor pieces, own `ArmorMaterial`) |
| **Blocks** | `sulfur_ore`, `buildable_grass_block`, `buildable_sand_block`, building blocks + "center" variants for wood/stone/metal/HQM, `wooden_building_stairs`, `vending_machine` (2-tall, facing), `drinkable_block` (debug), `crate` (9-slot container) |
| **Building** | Blueprint floor builds a 3×3 slab of building blocks with a "center" block, only on buildable surfaces and on a 3-block grid (checks centers at ±3 / ±6). Wall and stairs blueprints work the same way. The hammer upgrades twig→stone→metal→HQM using resources, and removes wooden pieces when sneaking. |
| **Thirst** | Values stored in a custom NBT compound injected into `Entity` by a mixin (`IEntityDataSaver`). `PlayerTickHandler` adds "thirst saturation" each tick based on horizontal speed, and at 5000 it removes 1 thirst. There's a HUD overlay (10 droplets), S2C sync payloads, and a respawn copy mixin. |
| **Vending machine** | The local copy has a newer custom-screen version (`VendingMachineBlock`, `VendingMachineBlockEntity`, `vending/VendingMachineTrade`, `VendingMachineScreen(Handler)`, `VendingTradePacket`). **GitHub only has the older `ResourceVendingMachineBlock` version.** The local work is not pushed. |
| **Crate** | Block entity with 9 slots and a custom screen. No loot table, no refill yet. |
| **Recipes / loot** | Only `low_grade_fuel` (shapeless) and the `sulfur_ore` loot table. Everything else is hand-written JSON. |

### 1.2 Problems found (to fix in Phase 0/1)

| # | Problem | Where | Fixed in |
|---|---|---|---|
| 1 | `VendingTradePacket` (a **common** class) imports `ClientPlayNetworking`. This can crash a **dedicated server** when the class loads. | `networking/VendingTradePacket.java` | P0.3 |
| 2 | `HammerItem` / blueprints import `net.minecraft.client.gui.screen.Screen` (for shift/control tooltips) in common code. Same risk. | `item/custom/*` | P0.3 |
| 3 | New players start with **thirst = 0** (`getInt` default). Respawn sets **10**, but the max is **20**. These are inconsistent. | `ThirstData`, `ServerPlayerEntityMixin` | P1.1 |
| 4 | `ThirstSaturationData.addSaturation` sends a packet **every tick for every player**, and the client never uses that value. | `ThirstSaturationData` | P0.6 / P1.1 |
| 5 | The speed thresholds in `PlayerTickHandler` don't match real speeds. `horizontalSpeed` grows by `distance × 0.6` per tick, so walking is ≈0.13 and sprinting ≈0.17. `> 0.13` fires for walking and `> 0.20` almost never fires. Jumping isn't detected at all. | `event/PlayerTickHandler` | P1.1 |
| 6 | Thirst at 0 does nothing (no damage or slowness). Drinking only exists through the debug `drinkable_block` while sneaking. | thirst | P1.1, P1.2 |
| 7 | The thirst HUD at `y - 53` overlaps the vanilla **air bubbles** row when underwater, and the mount-health row. | `ThirstHudOverlay` | P1.1 |
| 8 | Building blocks use `.strength(-1)`. Hardness −1 makes them unbreakable, but the explosion **resistance clamps to 0**, so TNT and creepers delete bases instantly. That needs a design decision (see P4.3). | `ModBlocks` | P4.3 |
| 9 | `wooden_building_stairs` is registered but has no blockstate/model/lang, so it shows as a missing texture. `blueprint_stairs` / `blueprint_wall` aren't in the creative tab. | `ModBlocks`, `ModGroups` | P0.4 / P0.5 |
| 10 | The custom `IEntityDataSaver` mixin attaches an NBT compound to **every entity**. Fabric's **Data Attachment API** is the supported way to do this now. | `mixin/ModEntityDataSaverMixin` | P0.6 |
| 11 | Inventory consumption code (`placeFloor`, upgrade…) is long, duplicated and fragile (manual slot loops, partial refunds). | blueprints / hammer | P4.1 |
| 12 | Tooltips and messages hard-code `§` colour codes in lang. Use `Formatting` on `Text` instead. | `en_us.json` | P0.4 |
| 13 | `ModMessages` has unused identifiers that duplicate the ones in the payload records. | `networking/ModMessages` | P0.5 |
| 14 | Local work (vending machine rewrite, newer files from 2026) isn't committed or pushed. | git | P0.1 |

---

## 2. Roadmap at a glance

| Phase | Theme | Main outcome |
|---|---|---|
| **0** | Foundation | Audit, version pin, client/server split, **DataGen for everything**, registry/player-data refactor, test tools |
| **1** | Survival core | Finished thirst, drinkable water, effects (radiation / bleeding / cold) + protection stats, food, medical, low gamma |
| **2** | Resources, gathering & crafting | Rust resources and components, tools, hemp and corn, furnaces, recycler, workbenches |
| **3** | Clothing & armor | Rust clothing sets with radiation/cold/projectile protection |
| **4** | Building (grid) | Refactored grid building, more pieces, building HP, tool cupboard, doors, key/keypad/card locks |
| **5** | Loot & world | Barrels, crates, loot tables, loot spawners, irradiated biome and monuments, keycard puzzles, bear (and other animals) |
| **6** | Economy | Finished NPC + player vending machines, scrap economy |
| **7** | Combat | Gun framework, ammo, the first guns, bows, explosives / raiding |
| **8** | Player experience | Corpses / backpacks (BodiesBodies-like), held-item rendering (Hold-My-Items-like), sleepers, **spawn mechanic** (future) |
| **9** | Polish & release | Balancing, art and sounds, translations, compatibility, Modrinth/CurseForge release |

**Suggested milestones**

- **M1 "Clean base"**: Phase 0 done. Everything is datagen'd and the dedicated server boots.
- **M2 "Naked on the beach"**: P1.1–P1.4, P2.1–P2.6. You can survive (thirst, water, food), gather and craft basic tools.
- **M3 "First base"**: Phase 4 core (P4.1–P4.7) + P3.1–P3.2. You can build a twig→stone base with a door and lock.
- **M4 "Roaming"**: Phase 5 + P1.5–P1.8 + Phase 6. Barrels, crates, radiation, bear, vending machines, keycards.
- **M5 "Pew pew"**: Phase 7. Guns, ammo, raiding.
- **M6 "Feels like Rust"**: Phase 8 + 9.

The phases are not strictly sequential. Phase 2 and Phase 4 can run in parallel once Phase 0 is done. Phase 7 needs P1.6 (bleeding) and P4.3 (building HP) for its raiding part.

---

## Phase 0 – Foundation

### P0.1 – Review what is already done & commit local work
- **Goal:** Know exactly what works, what's broken, and get the local work into git.
- **Steps:**
  1. Commit and push the local vending-machine rewrite and any other uncommitted files. GitHub is currently behind (the last commit there is *"ScreenHandler error RVM"*). Remove the old `ResourceVendingMachine*` classes if the new `VendingMachine*` ones replace them.
  2. Run `./gradlew build`, `runClient` **and** `runServer`. Write down every crash or warning.
  3. In-game smoke test: place every block, use every item, check the creative tabs, build floor/wall/stairs, upgrade with the hammer, open the crate, trade at the vending machine, watch the thirst bar while walking, sprinting and swimming.
  4. Go through the "Problems found" table in §1.2. Confirm each one and add anything new.
  5. Add `docs/STATUS.md` (or a section in the README) listing each feature as ✅ works / ⚠️ partial / ❌ broken.
- **Done when:** Git is up to date, the status list exists, and every problem has a task ID.
- **Depends on:** –  **Size:** S

### P0.2 – Pin the Minecraft / toolchain version
- **Goal:** Stop building on a moving target.
- **Description:** The mod targets `1.21` (`~1.21` accepts 1.21.x). **1.21.1** is protocol-compatible, has the bigger mod ecosystem (GeckoLib, Accessories/Trinkets, ModMenu, etc.) and contains no breaking API changes from 1.21. Versions 1.21.2+ change a lot of item, armor, recipe and rendering APIs. Recommendation: **move to 1.21.1 and stay there** until the main phases are done, then port once.
- **Steps:** Update `gradle.properties` (`minecraft_version=1.21.1`, matching Yarn build, Fabric API for 1.21.1, a **stable** Loom instead of `-SNAPSHOT`). Set `"minecraft": "~1.21.1"` in `fabric.mod.json`. Remove the template `"suggests": {"another-mod": "*"}` and set `homepage` to the GitHub repo. Rebuild and fix any compile errors.
- **Done when:** The build is green on the pinned versions and CI passes.
- **Depends on:** P0.1  **Size:** S

### P0.3 – Split client and server code
- **Goal:** Make dedicated-server crashes impossible to introduce by accident.
- **Steps:**
  1. Turn on Loom's split source sets: `loom { splitEnvironmentSourceSets(); mods { rustcraft { sourceSet sourceSets.main; sourceSet sourceSets.client } } }`. Move client-only code to `src/client/java` (`RustCraftClient`, HUD, screens, renderers, client packet receivers).
  2. Move `VendingTradePacket.send()` into a client-side helper (e.g. `client/network/ClientPackets`).
  3. Replace `Screen.hasShiftDown()` in item tooltips with a client-side helper, or pass the key state in with a small `ClientTooltipHelper`. With split source sets the compiler will refuse client imports in common code.
  4. Add a CI step that runs the server for a few seconds (`runServer` with `--nogui` and an auto-stop), or use the Fabric game test server (P0.8).
- **Done when:** `src/main` compiles without any `net.minecraft.client` import, and `runServer` starts cleanly.
- **Depends on:** P0.1  **Size:** M

### P0.4 – DataGen for everything (ToDo item #1)
- **Goal:** Replace hand-written JSON with generated JSON.
- **Description:** Fabric API's datagen writes into `src/main/generated`. Loom already adds that folder to resources because `configureDataGeneration` is on. You run it with `./gradlew runDatagen`.
- **Steps:**
  1. Create the package `net.rafa.rustcraft.datagen` with these providers, and register each one in `RustCraftDataGenerator` with `pack.addProvider(...)`:
     - `ModModelProvider extends FabricModelProvider`: `generateBlockStateModels` (simple cubes, `top/side` building blocks via `TexturedModel`/custom `Model`, ore, stairs) and `generateItemModels` (all `generated`/`handheld` item models).
     - `ModLangProvider extends FabricLanguageProvider` (`en_us`). Add a second one for `es_es` later. Generate names from the registries so a missing name fails the build.
     - `ModBlockLootTableProvider extends FabricBlockLootTableProvider`: `sulfur_ore` with fortune, `addDrop(self)` for normal blocks, `crate` dropping itself and not its contents.
     - `ModRecipeProvider extends FabricRecipeProvider`: move `low_grade_fuel` here. All future recipes go here too.
     - `ModBlockTagProvider` / `ModItemTagProvider`: `mineable/pickaxe`, `needs_stone_tool`, plus mod tags (`rustcraft:building_blocks`, `rustcraft:buildable_surfaces`, `rustcraft:center_building_blocks`, `rustcraft:radiation_protective`, …).
     - `ModDynamicRegistryProvider extends FabricDynamicRegistryProvider`, set up now for later: damage types (dehydration, radiation, bleeding), biomes, worldgen features.
     - Later: `ModAdvancementProvider`, `ModEntityLootTableProvider`, a `SimpleFabricLootTableProvider` for chest loot (`LootContextTypes.CHEST`), and a custom provider for vending trades and gun stats.
  2. Run datagen and **diff** the generated files against the existing hand-made ones. They should match or be better. Then **delete** the hand-made duplicates from `src/main/resources`.
  3. Keep complex Blockbench models (`vending_machine*.json`, `crate.json`) hand-made. Only generate their blockstate (`VariantsBlockStateSupplier` with facing/half) and the item model that points at the parent.
  4. Fix the `wooden_building_stairs` missing assets (`registerStairs` in the model provider, or remove the block if you're not using it). Add the missing lang keys.
  5. Remove the `§` colour codes from lang values and apply `Formatting` in code (`Text.translatable(key).formatted(Formatting.RED)`).
  6. Add a section to the README: *"After adding a block/item: register → add to providers → `runDatagen` → commit generated files."*
  7. CI: add a `runDatagen` step and fail if `git diff --exit-code src/main/generated` isn't clean, so generated files never go stale.
- **Done when:** Only the complex models, textures and `fabric.mod.json`/mixins are hand-written. `runDatagen` is reproducible, and the game looks identical to before.
- **Depends on:** P0.2 (do it on the pinned version)  **Size:** M

### P0.5 – Registration & code-structure cleanup
- **Goal:** Make adding the hundreds of future items painless.
- **Steps:**
  - Create helpers: `ModItems.register(name, factory, settings)`, `ModBlocks.register(name, factory, settings, withItem)`. Keep a list of registered entries so the creative tabs and datagen can iterate over them automatically.
  - Split the creative tabs by theme: *Resources*, *Components*, *Tools & Weapons*, *Clothing*, *Building*, *Medical & Food*, *Deployables*. Fill them from lists instead of by hand, which fixes the missing blueprints.
  - Add `ModTags`, `ModSounds` (register `SoundEvent`s and generate `sounds.json`), `ModDamageTypes`, `ModEffects`, `ModComponents` (custom `DataComponentType`s, see P1.7), `ModEntities`.
  - Remove the dead `ModMessages` and put all payloads in `network/ModPayloads` with one `register()`.
  - Use feature packages (`thirst`, `building`, `vending`, `loot`, `gun`…). Each has a static `init()` called from `RustCraft.onInitialize`.
- **Done when:** Adding a simple item takes one line plus a datagen entry. The creative tabs fill automatically.
- **Depends on:** P0.4  **Size:** M

### P0.6 – Player data via Fabric Data Attachments
- **Goal:** A clean, synced, persistent store for all Rust player stats (thirst, hydration timer, radiation, bleeding, comfort, wetness, …).
- **Steps:**
  1. Define a `RustPlayerData` record with a `Codec` (thirst, thirstExhaustion, radiationPoisoning, radiationLevel, bleeding, wetness, …). Register it with `AttachmentRegistry.<RustPlayerData>builder().persistent(CODEC).copyOnDeath()/initializer(...)`. Check what attachment features your Fabric API version has. Automatic sync arrived in later 1.21.x Fabric API versions. If you don't have it, keep your own S2C payload.
  2. Create one S2C payload `PlayerStatsSyncS2C`. Send it **only when a displayed value changes** (or at most every 10 ticks), never every tick.
  3. Migrate: on first load, read the old `rustcraft.rustcraft_data` NBT if it exists, move it to the attachment, then delete it. After that, remove `ModEntityDataSaverMixin`, `IEntityDataSaver` and `ServerPlayerEntityMixin`.
  4. Handle respawn: decide which values reset (Rust: thirst/hunger reset to partial values, radiation resets, bleeding resets).
- **Done when:** Thirst survives relog, death and dimension change, and no packets are sent while the value stays the same.
- **Depends on:** P0.5  **Size:** M

### P0.7 – Configuration & gamerules
- **Goal:** Balance values and on/off switches without recompiling.
- **Description:** Use a small JSON config (`config/rustcraft.json`, a hand-rolled Codec) or a library (owo-config, Cloth Config + ModMenu). Server-side values (thirst rate, damage, upkeep on/off, disable vanilla regen, force gamma…) are **synced to clients** where needed. Use gamerules for per-world toggles (`rustcraft:thirstEnabled`, `rustcraft:buildingDecay`, …).
- **Done when:** At least the thirst rates and the gamma toggle come from config.
- **Depends on:** P0.5  **Size:** S

### P0.8 – Developer tooling
- **Goal:** Faster iteration and safer refactors.
- **Steps:**
  - Add `/rustcraft` debug commands: `stats set <thirst|radiation|bleeding> <value>`, `loot roll <table>`, `building grid show`, `spawn crate <type>`.
  - Add **Fabric GameTest** (`fabric-gametest-api-v1`) tests for the pure logic: inventory consumption, grid snapping, thirst tick math, trade execution.
  - Keep a test world template in `run/` (git-ignored) with a flat buildable area.
  - Optional: Mod Menu integration for the config screen.
- **Done when:** You can set every stat with a command, and at least 5 game tests run in CI.
- **Depends on:** P0.6  **Size:** M

---

## Phase 1 – Survival core

### P1.1 – Finish the thirst system (ToDo: "+Thirst bar")
- **Goal:** Thirst works like the ToDo describes and has real consequences.
- **Design (from the ToDo):** Each tick, add to *thirst exhaustion*: **still +1**, **moving +2**, **running +7**, **jumping +2**, with a **maximum of 12 per tick**. When exhaustion reaches the threshold (currently 5000), thirst goes down by 1 and exhaustion resets. Thirst goes from 0 to 20 (10 droplets).
- **Steps:**
  1. Replace the speed thresholds with reliable signals. Use `player.isSprinting()` for running. For moving, check whether horizontal velocity is above about 0.01 (`getVelocity().horizontalLengthSquared()`, or the position delta since the last tick, which is more reliable on the server). For jumping, inject into `PlayerEntity.jump()` (or use `Stats.JUMP`) and set a per-tick flag. Add each bonus separately and cap the total at 12, as the ToDo says. Swimming and sneaking can use their own values from config.
  2. Add multipliers: hot biomes (temperature > 1.0) ×1.5, the nether ×2, wearing heavy armor, and so on. Keep them configurable.
  3. Consequences: at thirst 0, deal damage every N seconds with a custom `rustcraft:dehydration` damage type. Below 3 droplets, block sprinting like vanilla hunger does. Optional: thirst can also block natural health regen.
  4. Starting value: new players and respawns get a configurable start value (Rust respawns you partly hydrated). Fixes problem #3.
  5. "Saturation to decrease thirst": add a **hydration saturation** buffer, filled by drinks, that is used up before the visible thirst bar drops, like vanilla food saturation.
  6. HUD: draw above the hunger bar and move up a row when the air bubbles or mount health are visible. Hide it in creative/spectator. Optional: a small shake when thirst is low.
  7. Remove the debug `drinkable_block` once P1.2 lands, or keep it creative-only.
- **Done when:** Standing still for 10 minutes loses much less thirst than sprinting for 10 minutes. Dehydration hurts. The HUD never overlaps anything.
- **Depends on:** P0.6  **Size:** M

### P1.2 – Drinkable water (ToDo: "Drinkable water (fluid)")
- **Goal:** Water sources that restore thirst, with salt water vs fresh water like in Rust.
- **Description:** In Rust, sea water is salty (drinking it causes dehydration), while rivers, lakes and rain water are fresh. Purified water is safe.
- **Steps:**
  1. **Drinking from the world:** with an empty hand, sneak + right-click on water. Raycast for fluids (`RaycastContext.FluidHandling.SOURCE_ONLY`). Decide fresh vs salt from the biome: `#minecraft:is_ocean` / `#is_beach` → salt, otherwise fresh. This needs no new fluid and works everywhere.
  2. **Containers:** `water_bottle` (small, 0.5 L) and `water_jug` (5 L). Store the amount and `water_type` (fresh/salt/purified) in a data component. Use the vanilla potion water bottle → maps to fresh. Drinking restores thirst based on amount and type.
  3. **Custom fluids** (what the ToDo asks for): register `fresh_water`, `salt_water` (optional) and `purified_water` as `FlowableFluid`s with buckets (Fabric `FluidRenderHandlerRegistry` for rendering). Decide whether salt water should also *replace* ocean water through worldgen. That's a big change, so in the first version just use the biome rule from step 1.
  4. **Purification:** boil salt water in a furnace/campfire (bottle → purified bottle), or build a `water_purifier` deployable. Later add a `water_catcher` (fills slowly with rain) and a `water_barrel`.
  5. Salt water: drinking gives a little thirst but adds a "salty" exhaustion penalty, so it's a net loss.
- **Done when:** You can survive on rivers and bottles, ocean water hurts you, and containers show their contents in the tooltip.
- **Depends on:** P1.1  **Size:** L (custom fluids are the large part, so split them into P1.2a world drinking + containers and P1.2b fluids + purification)

### P1.3 – Force low gamma (ToDo: "Force gamma to be low")
- **Goal:** Nights and caves are actually dark, as in Rust (no full-bright cheating).
- **Steps:** Add a client mixin on `GameOptions`/`SimpleOption<Double> gamma` (or `LightmapTextureManager.update`) that clamps gamma to the server-provided maximum (default 0.0–0.5). Sync the maximum from server config on join. Hide or lock the slider in the video options screen and show a tooltip saying the server enforces it. Also consider clamping `nightVision` from other sources and the `darknessEffectScale`/`brightness` accessibility settings carefully, so accessibility options stay respected.
- **Done when:** Setting gamma to 100% in options has no effect on a server with the option on.
- **Depends on:** P0.7, P0.3  **Size:** S

### P1.4 – Food (ToDo: "Food (what's not already in vanilla)")
- **Goal:** Add Rust foods that vanilla doesn't have, and let food also affect hydration.
- **Item list:**
  - *Meat (raw / cooked / burnt):* bear meat, wolf meat (vanilla wolves drop it), deer meat, horse meat, fish fillet (vanilla fish can count as "small/medium fish"). Pork and chicken map to vanilla.
  - *Foraged:* **corn** (P2.7), **pumpkin** (vanilla), **potato** (vanilla), mushroom (vanilla), **blueberries** / other wild berries, **cactus flesh** (hydration +), apple (vanilla).
  - *Loot food:* can of beans, can of tuna, granola, chocolate bar, pickles, small water bottle, black raspberries. These come from food crates and barrels (Phase 5).
  - *Cooking:* raw → cooked in a furnace/campfire. **Burnt** happens when cooked meat is left in a Rust furnace/campfire for too long (P2.8). Burnt food is low value and gives a small hunger debuff.
- **Steps:** Create a `RustFoodItem` with a `FoodComponent` plus a custom `hydration` component (positive for cactus, cans and fruit, negative for salty food). Hook into `Item.finishUsing` to apply hydration to thirst. Add datagen models, lang, smelting/campfire recipes, and an item tag `rustcraft:foods/raw_meat`, etc.
- **Done when:** Every listed food exists, can be eaten, and some foods change thirst.
- **Depends on:** P1.1, P0.4  **Size:** M

### P1.5 – Radiation (ToDo: "radiation → custom biome to receive radiation")
- **Goal:** Places that irradiate you, with protection that matters.
- **Design (Rust-like):** There are two numbers. **Radiation level** is the environment's exposure rate (rads/s) where you stand. **Radiation poisoning** is what your body has accumulated. Poisoning goes up when exposure is higher than your *radiation protection*. It goes down slowly when you're out of radiation (anti-rad pills help). Above certain thresholds you lose health and regen. In Rust, monuments are rated low/medium/high radiation (Gas Station and Supermarket ≈ none, Water Treatment/Train Yard/Airfield ≈ medium, Launch Site/Military Tunnels ≈ high).
- **Steps:**
  1. **Sources:**
     - (a) An **irradiated biome** (`rustcraft:irradiated_wasteland`, created by the dynamic registry datagen). Adding biomes to overworld generation on Fabric 1.21 needs a library. Use **Biolith** or **TerraBlender** (pick one, and add it as a dependency in P0.2). The biome stores a base radiation level.
     - (b) **Zones** from structures and monuments (Phase 5): a `RadiationZone` defined by a structure's bounding box plus a level (low/med/high). Look it up from `StructureAccessor` with a per-player cache.
     - (c) Optional **radioactive blocks/items** (e.g. a `radioactive_barrel` that emits within a radius).
  2. Add a server tick every 20 ticks per player: compute exposure → apply protection → update poisoning → effects (thresholds 1–4: hunger drain, no health regen, damage with the `rustcraft:radiation` damage type).
  3. Add a HUD element: a radiation icon with level, like the Rust bottom-right status list (P1.9), and a Geiger counter sound clicking faster with exposure.
  4. Register a `StatusEffect` `radiation_poisoning` only for visuals (icon in the inventory). The real value stays in the attachment.
- **Done when:** Walking into the biome or zone with no protection gradually damages you, and a hazmat suit lets you stay much longer.
- **Depends on:** P0.6, P1.7  **Size:** L

### P1.6 – Bleeding (ToDo: "bleeding → when shot")
- **Goal:** Hits from guns, arrows, animals and sharp weapons can cause bleeding until you treat it.
- **Steps:**
  1. Add a `bleeding` value (in Rust, bleeding is a pool that drains slowly and deals damage over time) to the player attachment.
  2. Apply bleeding in `ServerLivingEntityEvents.ALLOW_DAMAGE`/`AFTER_DAMAGE`, depending on the damage-type tag `rustcraft:causes_bleeding` (arrows, bullets, bear/wolf attacks, sword/spear). Add bleed amount = damage × factor, reduced by armor.
  3. Each second: deal damage with the `rustcraft:bleeding` type and reduce the bleeding pool. Stop when it's empty.
  4. Add a visual: a blood-drop HUD icon, occasional red particles under the player, and maybe a red vignette.
  5. Cures: bandage, syringe, medkit (P1.8). Vanilla healing (golden apple, regeneration) **doesn't** stop bleeding.
- **Done when:** Getting shot by a skeleton makes you bleed, and a bandage stops it.
- **Depends on:** P0.6  **Size:** M

### P1.7 – Protection properties (ToDo: "→ property to add radiation and cold protection")
- **Goal:** A generic way to give items protection stats, as in Rust's clothing tables.
- **Description:** Define a **data component** `rustcraft:protection` with `{radiation: float, cold: float, projectile: float, melee: float, explosion: float, bite: float}` (only the fields you need at first: radiation and cold). Equipped armor sums its values. Add a tooltip line for each non-zero value with an icon. Use a `ProtectionCalculator.get(player, type)` helper that the radiation, cold, bleeding and gun systems read.
- **Steps:** Register the component type (`Registries.DATA_COMPONENT_TYPE`) with a codec and packet codec. Set default values on item `Settings` for mod armor. Optionally apply it to vanilla armor too (leather has cold protection, for example) via `DefaultItemComponentEvents.MODIFY`. Show the totals in the inventory screen next to the armor bar (stretch goal).
- **Done when:** The hazmat suit shows "Radiation +X" and "Cold +Y", and the systems use those values.
- **Depends on:** P0.5  **Size:** S–M

### P1.8 – Bandages, syringe & other medical (ToDo: "Bandages and Syringe")
- **Goal:** Rust-style healing items.
- **Items:**
  - **Bandage** (cloth ×4): 2 s use time, stops bleeding, heals a little instantly.
  - **Medical syringe** (cloth + metal fragments + low grade fuel): 1 s use, instant heal + heal over time, stops bleeding.
  - **Large medkit** (stretch): long use, big heal over time, stops bleeding.
  - **Anti-radiation pills**: removes radiation poisoning.
  - **Tea / herbal remedy** (stretch).
- **Steps:** Create `MedicalItem` with configurable use time (`getMaxUseTime`, `UseAction.BOW`/custom), with effects applied in `finishUsing`. Add a cooldown so it can't be spammed. Show a "healing" indicator in the HUD (P1.9). Add crafting recipes via datagen, and also make these items loot in medical crates (Phase 5). Optional: right-click another player to heal them, as in Rust.
- **Done when:** All the items work and can be crafted or looted.
- **Depends on:** P1.6, P1.5 (for pills)  **Size:** M

### P1.9 – Status HUD
- **Goal:** One coherent HUD for the Rust stats.
- **Description:** Rust shows a vertical list of status icons in the bottom-right corner (cold, wet, bleeding, radiation, building privilege, comfort…). Build a `StatusHud` that draws only the active statuses with value text, plus the thirst bar from P1.1. Make the position configurable.
- **Done when:** Bleeding, radiation, cold and later building privilege show up cleanly without overlapping.
- **Depends on:** P1.1, P1.5, P1.6  **Size:** S–M

### P1.10 – Cold / freezing (ToDo: "freezing → minecraft default")
- **Goal:** Cold that uses the vanilla freezing mechanic, made smarter.
- **Description:** Vanilla freezing (`frozenTicks`) only happens in powder snow. Leather armor (`#freeze_immune_wearables`) prevents it.
- **Steps:**
  1. Compute a **temperature** per player: biome temperature + time of night − (wet ? penalty) + nearby heat sources (campfire, furnace, torch in hand within N blocks) + elevation.
  2. If the temperature is below a threshold and cold protection (P1.7) isn't enough, increase `player.setFrozenTicks(...)` gradually. That reuses the vanilla frost overlay, slowness and freeze damage, which matches the ToDo ("minecraft default"). Let it go down near heat or with enough protection.
  3. Optional: being **wet** (in water/rain) amplifies cold. Track a `wetness` value in the attachment.
  4. Optional: heat (desert at midday) increases thirst (P1.1 multipliers) instead of hurting you.
- **Done when:** Standing in a snowy biome at night without clothes slowly freezes you, and a campfire or clothing prevents it.
- **Depends on:** P1.7  **Size:** M

### P1.11 – Optional survival extras (added, low priority)
- **Comfort:** being near a campfire, sitting in a chair or under a roof gives comfort, which boosts health regen. Ties into P1.9.
- **Disable vanilla natural regen** (config) so that medical items matter, as in Rust.
- **Calories vs hunger:** map Rust's calories onto the vanilla hunger bar (no new bar).

---

## Phase 2 – Resources, gathering & crafting

### P2.1 – Resource items (ToDo: "Resources (what's not already in vanilla)")
- **Goal:** The complete Rust resource set, reusing vanilla where it fits.
- **Table:**

| Rust resource | Status | Plan |
|---|---|---|
| Wood | ✅ `rustcraft:wood` | Comes from chopping vanilla logs with Rust tools (P2.3). Decide whether vanilla logs can also be converted. |
| Stone | ✅ `rustcraft:stone` | From stone nodes / mining vanilla stone with Rust picks |
| Metal ore | ❌ | `metal_ore` (block + raw item), smelted → **metal fragments** |
| Metal fragments | ✅ (`metal`) | Rename the display name to "Metal Fragments" (keep the id or migrate) |
| Sulfur ore / sulfur | ✅ | Add raw `sulfur_ore` item; smelting → sulfur |
| High quality metal ore / HQM | ✅ (HQM) | Add `hqm_ore` (block + raw item) → smelt to HQM |
| Scrap | ✅ | Source: barrels, crates, recycler |
| Cloth | ✅ | Source: **hemp** (P2.7), recycled rope/tarp/sewing kit |
| Animal fat | ✅ | Source: harvesting animals (P2.4) |
| Low grade fuel | ✅ | Recipe exists (3 fat + 1 cloth). Also from barrels and refining crude oil |
| Charcoal | ❌ (vanilla charcoal) | Use **vanilla charcoal**, produced by the Rust furnace when burning wood |
| Crude oil | ❌ | From oil barrels (P5.1) and later a pump jack. Refinery → low grade fuel |
| Gun powder | ❌ (vanilla gunpowder) | Use **vanilla gunpowder**. Add a Rust recipe: charcoal ×3 + sulfur ×2 → gunpowder ×? (workbench) |
| Explosives | ❌ | Gunpowder + low grade fuel + sulfur + metal fragments (Phase 7) |
| Leather | vanilla | Animal hide → vanilla leather |
| Bone fragments | ❌ (vanilla bone) | Use vanilla `bone`/`bone_meal`, or add `bone_fragments` for bone tools and arrows |
| Human skull / wolf skull | ❌ | Stretch (trophies) |
| Diesel fuel, horse dung, fertilizer | ❌ | Stretch |

- **Steps:** Register the missing items and blocks, generate ores via worldgen features (`PlacedFeature` + `BiomeModifications.addFeature`), and add smelting/blasting recipes and loot tables, all through datagen.
- **Done when:** Every ✅/❌ row has a working source and use.
- **Depends on:** P0.4, P0.5  **Size:** M

### P2.2 – Ore nodes (added, optional)
- **Goal:** Rust-style **ore nodes** (stone/metal/sulfur boulders on the surface), in addition to or instead of underground ores.
- **Description:** Add a multi-hit node block (or a small entity) that gives resources each hit and breaks after N hits, with a "sweet spot" bonus (the Rust glowing spot). Generate them on the surface via worldgen. This makes the early game feel like Rust without needing caves.
- **Depends on:** P2.1, P2.3  **Size:** M

### P2.3 – Tools (ToDo: "Tools")
- **Goal:** Rust gathering tools with tiers and gather rates.
- **List (by tier):**
  - **Rock** (starting tool: weak pick, axe and weapon all in one, no recipe or crafted from stone)
  - **Torch** (light, low damage, can light campfires)
  - **Stone hatchet / stone pickaxe** (wood + stone)
  - **Hatchet / pickaxe** (wood + metal fragments)
  - **Salvaged axe / salvaged icepick / salvaged hammer** (components + metal)
  - **Jackhammer / chainsaw** (stretch, need fuel)
  - Already existing: **Hammer**, **Building plan / blueprints**.
- **Steps:** Use custom `ToolMaterial`s (`STONE_TIER`, `METAL_TIER`, `SALVAGED_TIER`). Create a **`GatherTool` component**: `{wood_rate, stone_rate, ore_rate, flesh_rate}`. When breaking logs/stone/ore or harvesting corpses, the rate multiplies the drop count. Do this with `LootTableEvents.MODIFY` + a custom `LootFunction` `rustcraft:apply_gather_rate`, or with the `PlayerBlockBreakEvents.AFTER` event. Optional: vanilla tools get poor gather rates if you want to push players toward Rust tools (config).
- **Done when:** Each tool gathers at a different, configurable rate, and the rock is enough to start.
- **Depends on:** P2.1  **Size:** M

### P2.4 – Harvesting animals (added)
- **Goal:** Killing an animal leaves a corpse you harvest with a hatchet or knife for meat, fat, leather and bone, as in Rust.
- **Description:** First version: modify vanilla animal loot tables so they also drop `animal_fat` and raw meats, scaled by the weapon's `flesh_rate`. Advanced version: spawn a harvestable corpse entity on death (shares code with P8.1).
- **Depends on:** P2.3  **Size:** S (simple) / M (corpse)

### P2.5 – Components (ToDo: "Components")
- **Goal:** All Rust components as items, used in crafting and recyclable.
- **List (and recycler output, Rust values):**

| Component | Recycles into |
|---|---|
| Gears | scrap + metal fragments |
| Metal spring | 10 scrap |
| Metal pipe | 5 scrap + 10 metal fragments |
| Sheet metal | 8 scrap + 10 metal fragments |
| Tech trash | 20 scrap + 10 HQM |
| Road signs | 5 scrap + 10 metal fragments |
| Rope | 15 cloth |
| Sewing kit | 2 scrap + 10 cloth |
| Tarp | 50 cloth |
| Electric fuse | 2 scrap (also the monument puzzle item, P5.6) |
| Rifle body | 25 scrap + 20 HQM |
| SMG body | 15 scrap + 20 HQM |
| Semi-automatic body | 15 scrap + 20 HQM |
| Metal blade | scrap + metal fragments |
| Empty propane tank | scrap + metal fragments |
| CCTV camera / targeting computer | stretch (scrap + HQM + tech trash) |

- **Steps:** Register the items in a `ModComponentsItems` class with their own creative tab. Put recycler outputs in a data-driven JSON (`data/rustcraft/recycling/*.json`, generated by datagen) so P2.9 can read them. Loot sources come in Phase 5.
- **Done when:** All the components exist with textures, lang and recycle data.
- **Depends on:** P0.4, P0.5  **Size:** S–M (mostly textures)

### P2.6 – Workbenches & crafting progression (added)
- **Goal:** Rust progression through **Workbench Level 1/2/3**.
- **Description:** Each recipe needs a workbench tier (0 = by hand). Option A (simple): workbench blocks with their own crafting screen and a custom `RecipeType` `rustcraft:workbench_crafting` that has a `tier` field. Option B (Rust-like): a **crafting menu** that lists every craftable item, with a craft queue and timers (opened with a key), where your workbench tier is whether you're standing within N blocks of a bench. Option A comes first, B can come later.
- **Steps:** Register blocks `workbench_1/2/3` (multi-block models), a recipe type with serializer and datagen builder, and a screen. Remove or keep vanilla crafting for vanilla items (config). Optional: a craft queue with timers.
- **Done when:** Guns and higher tools can only be crafted at the right workbench.
- **Depends on:** P2.1, P0.4  **Size:** L

### P2.7 – Hemp plant & corn (ToDo: "Hemp plant", "Corn")
- **Goal:** Plants you can harvest and farm.
- **Hemp:** A wild `hemp_plant` generates in grassy biomes (`BiomeModifications` + random patch feature). Right-click/break → **cloth ×10** + chance of `hemp_seed`. A farmable crop `hemp_crop` (`CropBlock`, 4–8 stages) grows from seeds.
- **Corn:** A wild corn plant plus a 2-block-tall crop (`TallPlantBlock`-like crop). It drops `corn` (food, P1.4) and `corn_seed`.
- **Steps:** Create crop blocks with `AGE` property, block/item models via datagen (`registerCrop`), loot tables with `BlockStatePropertyLootCondition` for mature drops, worldgen features, and a compostable/bone-meal registry. Stretch: Rust genetics (G/Y/H/W/X genes on seeds), the planter box, and the 1.21 planter + sprinkler system.
- **Done when:** You can find hemp in the wild, turn it into cloth, and farm both plants.
- **Depends on:** P0.4  **Size:** M

### P2.8 – Furnaces, campfire & refinery (added)
- **Goal:** Rust smelting that uses wood as fuel and produces charcoal.
- **Description:** **Rust furnace** (small): wood fuel, smelts ores, outputs charcoal as a by-product, cooks meat (with burnt meat if left in). **Large furnace** (stretch): more slots. **Small oil refinery**: crude oil → low grade fuel. **Campfire**: cooks food and gives heat (P1.10). The vanilla campfire can be reused with an extra burn mechanic.
- **Steps:** Write a `BlockEntity` with a custom tick (fuel, multiple input slots, output slots, charcoal by-product). Add a screen, recipes using the vanilla `SMELTING` type or a custom one, and datagen.
- **Depends on:** P2.1  **Size:** M–L

### P2.9 – Recycler (added, key to the Rust loop)
- **Goal:** Put components in, get scrap and resources out.
- **Description:** A deployable/monument `recycler` block: 6 input slots, 6 output slots, an on/off button, processing one item every ~5 s. It reads the recycling JSON from P2.5 and also returns ~50% of a crafted item's ingredients for items without data. Recyclers spawn at monuments in Phase 5, and can be made craftable in config.
- **Depends on:** P2.5  **Size:** M

### P2.10 – Research table & blueprints (added, optional)
- **Goal:** Rust's item **blueprint** unlocking. You research an item with scrap to learn its recipe permanently.
- **Description:** Store known blueprints in the player attachment, and filter workbench recipes by known blueprints. The research table consumes the item plus scrap. **Note:** the name clashes with the existing "blueprint" building items. Rename those to **Building Plan** (the Rust name) during P4.1.
- **Depends on:** P2.6, P0.6  **Size:** M

---

## Phase 3 – Clothing & armor

### P3.1 – Clothing framework (ToDo: "Clothing")
- **Goal:** Rust clothing with the protection values from P1.7.
- **Description:** Minecraft has 4 armor slots; Rust has more (hat, mask, shirt, jacket, pants, gloves, boots). **Decision:** start with 4 slots and map each Rust piece to the nearest slot. Later you can add extra slots with an accessories library (Accessories / Trinkets) for gloves and the second torso layer.
- **Steps:** Create a `ModArmorMaterials` helper that takes (name, durability multiplier, defense per slot, protection component). Datagen generates item models. Add armor textures (`textures/models/armor/<name>_layer_1/2.png`), or use GeckoLib armor models for non-standard shapes (coffee can helmet, road sign vest).
- **Depends on:** P1.7  **Size:** M

### P3.2 – Clothing sets
- **Sets (craft tier in brackets):**
  - **Burlap set** (0): shirt, trousers, shoes, headwrap. Small cold protection. Made from cloth.
  - **Hide set** (0): hide poncho, pants, boots, halterneck. Better cold protection, from leather.
  - **Bone armor** (1): helmet + chestplate, uses bone.
  - **Wood armor** (1): chestplate, pants, helmet.
  - **Road sign armor** (1–2): road sign jacket + kilt, coffee can helmet.
  - **Metal armor** (2–3): metal facemask, metal chestplate. High projectile protection.
  - **Hazmat suit** (✅ exists): add its protection values (high radiation, medium cold) and an optional full-set bonus.
  - **Arctic suit**, **heavy plate** (stretch).
- **Done when:** Each set has a recipe, textures and protection values, and appears in loot tables.
- **Depends on:** P3.1, P2.5  **Size:** L (mostly art)

---

## Phase 4 – Building (grid)

> The ToDo lists this as "Building mechanics (grid)". A first version exists (P0.1 review). This phase turns it into a solid system.

### P4.1 – Refactor the building system
- **Goal:** Replace the four big hand-coded classes with shared, testable pieces.
- **Steps:**
  1. Create `BuildingGrade` (enum or registry): TWIG, WOOD, STONE, METAL, ARMORED (= HQM). Each has a block pair (edge, center), upgrade cost, HP and sound. Rust names: Twig → Wood → Stone → Sheet Metal → Armored. The existing code starts at "wooden", so decide whether twig is a separate first grade.
  2. Create `BuildingPieceType`: FOUNDATION, FLOOR, WALL, DOORWAY, WINDOW, WALL_FRAME, STAIRS, ROOF (more later). Each defines its **footprint** (relative positions of the edge blocks and the center) and **placement rules**.
  3. Create `BuildingGrid`: snapping math (the current 3-block cell with centers on a 3-grid relative to the first foundation). Store each base's grid origin in a `BuildingData` (block entity in the center block, or a chunk/world attachment) so separate bases don't need to share one global grid.
  4. Create `InventoryUtil.consume(player, item, count)` / `has(…)` / `refund(...)` to replace the manual slot loops (fixes problem #11). Creative players don't consume anything.
  5. Merge the blueprints into one **Building Plan** item with a selected piece (sneak + right-click or a radial menu to switch, as the current tooltip already describes), stored in an item data component.
  6. Unit/game tests for snapping and footprints.
- **Done when:** Floor, wall and stairs behave as before, but through the shared code, and each class is under ~200 lines.
- **Depends on:** P0.5, P0.8  **Size:** L

### P4.2 – Placement preview (ghost)
- **Goal:** See where a piece will go before placing it.
- **Description:** A client-side render (`WorldRenderEvents.AFTER_TRANSLUCENT`) of translucent blocks for the footprint, green when valid and red when invalid. Validity is computed client-side with the same `BuildingGrid` code, and the server re-checks. Rotate with a key (R, as in Rust).
- **Depends on:** P4.1, P0.3  **Size:** M

### P4.3 – Building HP, damage & destruction
- **Goal:** Buildings can be damaged and destroyed according to grade, not just unbreakable or deleted by TNT (problem #8).
- **Steps:** Store HP per piece in the center block's block entity. Every edge block points to its center. Intercept damage: melee hits (`AttackBlockCallback`) do small, grade-dependent damage (**soft side** takes more, stretch). Explosions use a custom `ExplosionBehavior`/mixin so vanilla explosions don't delete the blocks, and instead apply damage to the piece. When HP hits 0, the whole piece is removed. The hammer shows HP (actionbar) and **repairs** with materials. Rust HP references: wood ~250, stone ~500, sheet metal ~1000, armored ~2000 (scale to MC).
- **Depends on:** P4.1  **Size:** L

### P4.4 – More building pieces
- **Goal:** The Rust building pieces.
- **List:** foundation (square), **triangle foundation/floor** (hard on a block grid, so stretch or use slab-stairs approximations), wall, half wall, low wall, **doorway**, **window** (+ window bars / embrasure), **wall frame** (+ garage door / shopfront), floor, floor frame/hatch, stairs (U-shaped / L-shaped), roof, ramp. **Pillars are gone in Rust, so skip them.**
- **Depends on:** P4.1  **Size:** L (one task per 2–3 pieces)

### P4.5 – Tool Cupboard & building privilege (added, core to Rust)
- **Goal:** Stop other players from building or editing next to your base.
- **Steps:** Add a `tool_cupboard` block (1000 wood in Rust) with an authorized player list (right-click to authorize yourself, clear list). Its **privilege zone** is the base footprint plus a margin (or simply a radius around the TC as a first version). Inside someone else's zone you can't place, upgrade, remove or pick up pieces. Show a "Building Blocked" / "Building Privilege" status in the HUD (P1.9). Store the list in a block entity. Use a server-side spatial index of TCs per chunk for fast lookup.
- **Depends on:** P4.1, P1.9  **Size:** M

### P4.6 – Upkeep & decay (added, optional, config-off by default in singleplayer)
- **Goal:** Bases need resources in the TC, otherwise they decay.
- **Description:** The TC inventory holds wood, stone, metal and HQM. Every upkeep period, it consumes a fraction of the base's total building cost. If it can't pay, pieces lose HP over time (Rust decay without upkeep: twig ~1 h, wood ~3 h, stone ~5 h, sheet metal ~8 h, armored ~12 h, so scale to MC days).
- **Depends on:** P4.3, P4.5  **Size:** M

### P4.7 – Doors (added, needed for every lock task)
- **Goal:** Rust doors that fit doorways.
- **List:** wooden door, sheet metal door, armored door, double doors (stretch), garage door (stretch), **ladder hatch** (stretch).
- **Description:** Custom door blocks (use vanilla `DoorBlock` behaviour, but with HP from P4.3, opened by hand). They can only be placed in a doorway piece. Each door has a **lock slot**: `NONE | KEY_LOCK | CODE_LOCK | CARD_READER` (block entity).
- **Depends on:** P4.4 (doorway), P4.3  **Size:** M

### P4.8 – Doors with keypad (ToDo: "Doors with keypad")
- **Goal:** Rust **code lock**.
- **Steps:** Add a `code_lock` item placed on a door (right-click). Its block entity data holds the owner, a 4-digit code, a guest code, and authorized and guest lists. Screens: a keypad GUI (digits + enter). Using a door with a code lock while unauthorized opens the keypad. A wrong code shocks you (small damage, cooldown) and plays a sound. The owner can change the code, lock/unlock, and remove the lock. It sends the code via a C2S packet, which the server validates and **rate-limits** (no brute force).
- **Done when:** Only authorized players open the door, and authorization persists across restarts.
- **Depends on:** P4.7  **Size:** M

### P4.9 – Key lock (added, cheap and early-game)
- **Goal:** Rust **key lock**: craft it, place it, and get keys that match its lock id.
- **Description:** The lock stores a random `lockId`, and a key item stores the same `lockId` in a data component. Holding any matching key in your inventory opens it.
- **Depends on:** P4.7  **Size:** S

### P4.10 – Doors with cards (ToDo: "Doors with cards") → see **P5.7**
Card doors are a monument mechanic in Rust, so they live in Phase 5 next to the keycards and fuses. They reuse the `lock slot` = `CARD_READER` from P4.7.

### P4.11 – Deployables (added)
- **Goal:** Basic Rust deployables placed inside bases.
- **List:** **Storage** (small wooden box / large wood box; the existing `crate` block can become the small box), sleeping bag (P8.4), furnace (P2.8), workbench (P2.6), TC (P4.5), barricades, ladders, shelves (stretch), traps (shotgun trap, flame turret, auto turret — stretch, Phase 7).
- **Depends on:** P4.5 (privilege for pick-up)  **Size:** M

---

## Phase 5 – Loot & world

### P5.1 – Barrels (ToDo: "Barrels", "Barrel resources")
- **Goal:** Breakable roadside barrels that drop loot, the core of Rust's early game.
- **Types:**
  - **Blue barrel / yellow barrel**: scrap, metal fragments, components (gears, springs, pipes, rope, sewing kits, tarps, road signs, sheet metal), low grade fuel, sometimes ammo or tools.
  - **Oil barrel** (red): **crude oil** + low grade fuel.
  - **Diesel barrel** (stretch).
- **Steps:**
  1. Add barrel blocks (Blockbench model, with breaking particles and sound) that are **broken by hitting** (low hardness or a hit counter, as in Rust where it takes a few hits). They don't drop themselves.
  2. Loot tables `rustcraft:barrels/blue`, `…/oil` (datagen `SimpleFabricLootTableProvider` with `LootContextTypes.BLOCK`, or the block loot of the barrel block). This is the **"barrel resources"** list. Keep the weights balanced so scrap is the most common.
  3. Worldgen: generate barrels along **roads** (if you add roads), near villages, at monuments, and scattered in plains/deserts (a custom `Feature`). Respawning works through loot spawners (P5.3).
- **Done when:** You can farm barrels for scrap and components in a new world.
- **Depends on:** P2.5, P0.4  **Size:** M

### P5.2 – Boxes / crates (ToDo: "Boxes (military... etc)")
- **Goal:** All Rust loot crates.
- **Types:** **Basic crate** (brown, wooden), **tool box**, **food crate**, **medical crate**, **military crate** (green, needs blue-tier monuments), **elite crate** (red-tier monuments / Launch Site), **locked crate** (timed: start it and wait ~15 min, defend it; for the Oil Rig, Cargo Ship and Chinook drops), **underwater crate** (stretch), **mine crate** (stretch).
- **Steps:**
  1. Generalise the existing `CrateBlock`/`CrateBlockEntity` into a `LootCrateBlock` with a `CrateType` (loot table id, slot count, model).
  2. Loot is generated **on first open** (`LootableContainerBlockEntity` + `setLootTable`, which vanilla already supports).
  3. Rust behaviour: once emptied (or after a timeout after first open), the crate **disappears**, and a loot spawner (P5.3) re-creates it later.
  4. Write each crate's loot table in datagen with the weights by tier (basic → tools, ammo, components; military → guns/weapon parts, HQM, medical; elite → rifle bodies, high-tier guns, C4/rockets, armor).
  5. Locked crate: a timer block entity, HUD countdown, and it opens when the timer ends.
- **Done when:** Every crate type exists, has its own loot table and visual, and respawns.
- **Depends on:** P5.1 (same loot pipeline), P2.5  **Size:** L

### P5.3 – Loot spawners
- **Goal:** Barrels and crates come back, as in Rust.
- **Description:** A hidden `loot_spawner` block (invisible, placed in structures) that holds `{type: barrel|crate, pool: [...], respawn: 20-30 min}`. It re-spawns its entity or block when the old one is gone and no player is within N blocks. Monuments (P5.5) place these inside their structure NBT.
- **Depends on:** P5.1, P5.2  **Size:** M

### P5.4 – Irradiated biome (ToDo: "radiation → custom biome")
- **Goal:** The custom biome where radiation is applied.
- **Steps:** Write the biome JSON via `FabricDynamicRegistryProvider` (grey/green fog and sky, sparse dead trees, coarse dirt, gravel, ash particles). Add it to overworld generation with Biolith/TerraBlender (P1.5). Its radiation level comes from config. Generate more barrels and better loot spawners inside it so the risk pays off.
- **Depends on:** P1.5  **Size:** M

### P5.5 – Monuments (added, the main place for crates, recyclers, radiation and card doors)
- **Goal:** Rust **monuments** as Minecraft structures.
- **Plan:** Build them in a creative world, save them with structure blocks (`.nbt`), and generate them with **jigsaw structures** (structure + template pool + structure set JSON, all datagen'd). Start with the simple ones:
  1. **Gas Station** (no radiation, recycler, green card room) – first.
  2. **Supermarket** (no radiation, green card spawns).
  3. **Satellite Dish / Dome** (low radiation).
  4. **Water Treatment / Train Yard / Airfield / Power Plant** (medium radiation, blue card rooms).
  5. **Military Tunnels / Launch Site** (high radiation, red card + elite crates).
  6. **Outpost / Bandit Camp** (safe zones with NPC vending machines, P6.2).
- Each monument defines a `RadiationZone` level (P1.5), loot spawners (P5.3), and card doors/fuses (P5.6/P5.7).
- **Depends on:** P5.3, P1.5  **Size:** XL (one task per monument)

### P5.6 – Cards (ToDo: "Cards")
- **Goal:** Green, blue and red **keycards**.
- **Description:** Keycard items with **durability** (each swipe uses some). Green cards spawn at no-radiation monuments, blue in rooms opened by green, red in rooms opened by blue (the Rust progression chain). Also add the **electric fuse** (P2.5) that has to be inserted into a **fuse box** to power the card reader, with a switch-order puzzle (stretch).
- **Depends on:** P2.5  **Size:** S

### P5.7 – Doors with cards (ToDo: "Doors with cards")
- **Goal:** Card reader doors at monuments.
- **Description:** A `card_reader` block next to a monument door (P4.7 door types, unbreakable when generated in a structure). Swiping the right colour keycard (only while the reader is powered by a fuse or redstone) opens the door for N seconds, then it closes. The reader colour is a blockstate. Use a different texture from player-placed doors.
- **Depends on:** P5.6, P4.7  **Size:** M

### P5.8 – Bear (ToDo: "Bear") and other wildlife
- **Goal:** Rust's dangerous **bear** (and later wolf, boar, stag, chicken, horse).
- **Bear spec:** ~400 HP in Rust (scale to MC, for example 40–50 HP), fast, neutral-aggressive. It attacks if you get close or hit it. Its attack causes **bleeding** (P1.6). Drops on harvest: bear meat ×N, animal fat, leather, bone. Spawns in forests and taiga. Optional polar bear variant.
- **Steps:** Create a `BearEntity extends HostileEntity` (or `AnimalEntity` + anger) with goals: `MeleeAttackGoal`, `RevengeGoal`, a custom "aggro if a player is within 8 blocks for 3 s", `WanderAroundFarGoal`. Model: Blockbench → vanilla `EntityModel` export, or **GeckoLib** (recommended if you'll animate guns too; decide in P7.1). Add spawn rules (`BiomeModifications.addSpawn`), sounds, a spawn egg, an entity loot table (datagen), and attributes.
- **Other animals (stretch):** use vanilla wolves (make them hostile at night?), pigs/hoglins as "boar", a new stag/deer, vanilla chickens and horses.
- **Depends on:** P1.6, P2.4  **Size:** M–L

### P5.9 – Roads & world flavour (added, stretch)
Simple roads between monuments (jigsaw pieces), road signs to harvest (a source of the road-sign component), and abandoned cars as loot spots.

---

## Phase 6 – Economy

### P6.1 – Finish the vending machine (ToDo: "Vending machines")
- **Goal:** A stable, data-driven vending machine.
- **Steps:**
  1. Finish the local rewrite (custom screen + `VendingTradePacket`) after reviewing it in P0.1.
  2. Load trades from datapack JSON (`data/rustcraft/vending/<shop>.json`, with a Codec for `VendingMachineTrade`, generated by datagen) instead of `TradeUtil`. Each machine block entity stores which shop id it uses.
  3. Server-side validation for every buy: re-check the price in the player inventory, stock, and that the player is within reach. Make it atomic (no duplication). Use `InventoryUtil` from P4.1.
  4. Show stock, "out of stock" and "not enough items" states (the lang keys already exist).
  5. Remove `BaseVendingMachineMerchant`/`TradeUtil` if they're no longer used (vanilla merchant approach).
- **Done when:** You can buy from a machine on a dedicated server, the trades come from JSON, and there's no way to duplicate items.
- **Depends on:** P0.1, P0.3  **Size:** M

### P6.2 – NPC vending (Outpost / Bandit Camp)
- **Goal:** Fixed shops at safe-zone monuments (resource exchange, component shop, weapons, building supplies, as in Rust's Outpost).
- **Description:** Indestructible vending machines with infinite stock or stock that restocks over time. They're placed in Outpost/Bandit Camp structures (P5.5). Optional: a safe-zone rule (no PvP, hostile turrets). The scrap → resources trades from the current `TradeUtil` move here.
- **Depends on:** P6.1, P5.5  **Size:** M

### P6.3 – Player vending machines
- **Goal:** Players run their own shops, as in Rust.
- **Description:** The owner opens the admin view, puts goods in the storage, and creates sell orders ("X of item A for Y of item B"). Customers see the shop view. Payment goes into the machine storage. It can be broadcast on a map (stretch).
- **Depends on:** P6.1  **Size:** M

---

## Phase 7 – Combat

### P7.1 – Gun framework (ToDo: "Guns (pain in the ass)")
- **Goal:** One reusable system so each new gun is mostly data plus art.
- **Architecture:**
  - `GunItem` + `GunStats` (JSON per gun in `data/rustcraft/guns/*.json`, datagen'd): damage, range and damage falloff, fire mode (semi/auto/burst/pump/bolt), RPM, magazine size, compatible ammo tag, reload time, recoil pattern (vertical/horizontal per shot), spread (hip/ADS/moving), ADS zoom, sounds.
  - **Item data components**: `loaded_ammo` (count + type), `attachments` (stretch), `fire_mode`.
  - **Client:** input handling via a mixin on `MinecraftClient.handleInputEvents`/`doAttack` so left-click fires instead of mining, and holding the button keeps firing for full-auto. Right-click is ADS (FOV change and slower movement), R is reload (keybinding). Each shot sends a C2S `FireGunC2S(seq, aimYaw, aimPitch)`. Client-side recoil applies camera kick, plus a muzzle flash and shell particles.
  - **Server:** validate the fire rate (time since last shot), ammo, and that the player is holding the gun. Do a **hitscan** raycast (`ProjectileUtil.raycast` for entities + world raycast for blocks) with spread, or spawn a fast `BulletEntity` for travel time and drop (better for snipers and arrows). Damage uses the `rustcraft:bullet` damage type (tagged `causes_bleeding`, bypasses some vanilla armor). Head-shot multiplier when the hit is above eye level minus a small margin. Rust's projectile protection (P1.7) reduces damage.
  - **Rendering:** a first-person model (Blockbench JSON item model with `display` transforms, or GeckoLib animated items for reload animations). Also a third-person pose: holding the gun with both arms (via a `BipedEntityModel` mixin; ties into P8.2).
  - **Library decision:** GeckoLib (animations for guns and the bear) is a big but common dependency. Without it, guns are static models with procedural "kick" animations. **Recommended:** start without it, add it if the static models feel bad.
- **Done when:** A test gun shoots, reloads, uses ammo, has recoil, and works in multiplayer on a dedicated server.
- **Depends on:** P0.3, P1.6, P1.7  **Size:** XL (split it: P7.1a input+packets, P7.1b server hitscan+damage, P7.1c recoil/ADS/HUD, P7.1d rendering)

### P7.2 – Ammunition (ToDo: "Ammunition")
- **Goal:** Rust ammo types, crafted at workbenches.
- **List:** pistol bullet, 5.56 rifle ammo, 12 gauge buckshot, 12 gauge slug, handmade shell, incendiary/HV variants (stretch), arrows (wooden arrow = vanilla arrow, **high velocity arrow**, **fire arrow**), rockets (P7.5).
- **Steps:** Ammo items with an `ammo` item tag per caliber, recipes (gunpowder + metal fragments, made at the workbench, via datagen), and a HUD ammo counter (magazine / reserve). Add ammo to loot tables (barrels: handmade shells and pistol ammo; military crates: rifle ammo).
- **Depends on:** P7.1, P2.1, P2.6  **Size:** S–M

### P7.3 – First guns
- **Order (simplest first):** **Eoka pistol** (single shot, random misfire, made of pipe and wood). **Waterpipe shotgun** (single shell, spread). **Revolver** (semi, 8). **Semi-automatic pistol (P2)**. **Pump shotgun**. **Thompson / custom SMG** (auto, pistol ammo). **Semi-automatic rifle**. **Assault rifle (AK)** (auto, recoil pattern). **Bolt action rifle** (scope). **M249/LR-300/MP5** (stretch).
- Each gun = JSON stats + model + sounds + recipe (needing the matching body component: semi body / SMG body / rifle body).
- **Depends on:** P7.1, P7.2, P2.5  **Size:** M per 2 guns

### P7.4 – Bows, crossbow & melee (added)
- **Description:** **Hunting bow** (a vanilla bow reskin with Rust stats), **crossbow** (vanilla crossbow variant), **compound bow** (stretch). Melee: **bone club, bone knife, wooden spear, stone spear, machete, salvaged sword, salvaged cleaver, longsword** (as `SwordItem`s with damage that can cause bleeding). The knife has a high `flesh_rate` for harvesting (P2.3/P2.4).
- **Depends on:** P1.6, P2.3  **Size:** M

### P7.5 – Explosives & raiding (added; needs P4.3)
- **Description:** **Beancan grenade** (unreliable), **satchel charge** (can dud), **timed explosive (C4)**, **rocket launcher + rockets**, **F1 grenade**. Each one deals **building damage** per grade (Rust raid tables) through the P4.3 damage pipe, with little or no terrain destruction (it shouldn't crater the vanilla world). They're crafted from explosives and tech trash at workbench 3.
- **Depends on:** P4.3, P7.1  **Size:** L

### P7.6 – Traps & turrets (stretch)
Shotgun trap, flame turret, auto turret (needs a gun and ammo, authorization list like the TC), landmine, bear trap (also a bleeding source).

---

## Phase 8 – Player experience

### P8.1 – Corpses / death backpack (ToDo: "BodiesBodies mod")
- **Goal:** When a player dies, their items stay in a **lootable corpse** (Rust: a player corpse that turns into a backpack) instead of scattering on the ground.
- **Steps:** Add a `PlayerCorpseEntity` that stores the full inventory (main + armor + offhand), the owner name/UUID and the skin. It renders as a lying player model with the name tag. Right-click opens a container screen to loot it. After N minutes it turns into a **backpack** entity (smaller model, same inventory), and it despawns after M minutes. Cancel vanilla death drops with `ServerLivingEntityEvents.AFTER_DEATH` + `ALLOW_DEATH`, or a mixin on `dropInventory`. Respect the `keepInventory` gamerule. It's also reused for harvestable animal corpses (P2.4).
- **Done when:** You can go back to your body and take your stuff, and so can other players.
- **Depends on:** P0.3  **Size:** M–L

### P8.2 – Held items rendering (ToDo: "Hold my items mod")
- **Goal:** Nicer first- and third-person poses for held items, especially two-handed guns, tools and torches.
- **Description:** Option A: implement only what RustCraft needs. This is a `BipedEntityModel`/`PlayerEntityRenderer` mixin, or `ArmPose` customisation through Fabric's `LivingEntityFeatureRenderer`. It makes guns two-handed, aimed when ADSing, and puts a torch in a raised hand. Option B: declare compatibility with the existing **Hold My Items** mod (optional dependency, `"suggests"` in `fabric.mod.json`) and make sure RustCraft items work with it. **Recommended:** A for guns (it's needed anyway in P7.1d) plus B as a suggestion.
- **Depends on:** P7.1 (for guns), P0.3  **Size:** M

### P8.3 – Sleepers (added, stretch)
When a player logs out, a sleeping player body stays in the world and can be killed and looted (Rust behaviour). It's a server-config option. It shares the entity code with P8.1.

### P8.4 – Spawn mechanic (ToDo: "Future: Spawn mechanic (choose beds or spawn on the shore)")
- **Goal:** Rust respawning: choose a sleeping bag or bed, or respawn randomly on a **beach**.
- **Steps:**
  1. Add a `sleeping_bag` deployable (cheap, cloth). Each bag/bed has a name and an owner, and the owner can share it. **Cooldowns:** sleeping bag ~5 min, bed ~2 min (config). Bags near each other share cooldowns.
  2. Replace the vanilla death screen with a **respawn screen** listing your bags/beds with cooldown timers and a "Respawn on beach" button (custom `Screen` + C2S packet).
  3. **Beach spawn:** pick a random position in a `#minecraft:is_beach` biome within the world border. Cache candidate positions at world load or search asynchronously.
  4. Respawn state: start with a **rock + torch** (+ bandage) only, with reduced thirst/hunger/health (P1.1/P0.6).
- **Done when:** Dying shows the screen, bags work with cooldowns, and a fresh spawn lands on a random beach with starter items.
- **Depends on:** P8.1, P0.6, P2.3  **Size:** L

### P8.5 – Teams (added, stretch)
A simple team system (invite/accept). Teammates are auto-authorized on the team leader's TC and code locks, can see each other on the map, and get no friendly-fire damage (config).

### P8.6 – Map / compass HUD (added, stretch)
A Rust-like map item showing monuments, your bags and your teammates. A compass bar at the top of the screen.

---

## Phase 9 – Polish & release

### P9.1 – Balancing pass
Play through a full "wipe" (new world → first base → monuments → guns) and tune the config values, loot weights, gather rates and HP. Write the final numbers into the datagen defaults.

### P9.2 – Art & audio
Make a list of every missing or placeholder texture, model and sound. Keep a consistent pixel-art style (16×16, Rust's muted palette). Generate `sounds.json` with datagen and add subtitles.

### P9.3 – Localisation
Add `en_us` (complete) and `es_es` via `FabricLanguageProvider`. Every text goes through translation keys.

### P9.4 – Compatibility & performance
Test with Sodium, Iris, ModMenu, REI/EMI (a recipe plugin for the workbench recipe type and the recycler), and Jade/WTHIT (show building HP and grade). Profile the per-player tick handlers (thirst, radiation, cold) with Spark. Batch them every 10–20 ticks instead of every tick.

### P9.5 – Release
Fill in the README (features, screenshots, required dependencies), CHANGELOG, a Modrinth/CurseForge page, versioning (`0.x` until feature-complete), and automated publishing from CI on tag (e.g. `mc-publish` GitHub Action). Write a wiki or `docs/` page per feature.

---

## 10. Cross-cutting decisions to make early

| # | Decision | Options | Recommendation | Decide in |
|---|---|---|---|---|
| D1 | MC version | stay 1.21 / 1.21.1 / latest | **1.21.1**, port later | P0.2 |
| D2 | Player data storage | custom mixin NBT / Fabric attachments / Cardinal Components | **Fabric attachments** | P0.6 |
| D3 | Overworld biome injection | TerraBlender / Biolith | Either. **Biolith** is Fabric-native | P1.5 |
| D4 | Animation library | none / GeckoLib | Start **none**, re-evaluate at P5.8/P7.1 | P5.8 |
| D5 | Extra clothing slots | 4 vanilla slots / Accessories / Trinkets | **4 slots first** | P3.1 |
| D6 | Vanilla crafting & items | keep / restrict | Keep, with config to restrict | P2.6 |
| D7 | Wood/stone items | custom `rustcraft:wood/stone` / vanilla logs and cobblestone | Keep custom, convert vanilla logs → wood | P2.1 |
| D8 | Twig grade | separate first grade / start at wood | Separate (Rust-accurate) | P4.1 |
| D9 | Bullets | hitscan / projectile entity | Hitscan for pistols/rifles, entity for arrows and rockets | P7.1 |
| D10 | Ocean water | biome rule / replace with salt-water fluid | Biome rule first | P1.2 |

---

## 11. ToDo traceability (nothing removed)

| ToDo line | Covered by |
|---|---|
| Make DataGen for everything instead of doing manual .json files | **P0.4** (+ rule 0.3.1, CI check) |
| Resources (what's not already in vanilla) | **P2.1**, P2.2 |
| Building mechanics (grid) | **P4.1–P4.4**, P4.5, P4.6, P4.11 |
| Barrels | **P5.1** |
| Vending machines | **P6.1**, P6.2, P6.3 |
| Barrel resources | **P5.1** (loot tables), P2.5 |
| Boxes (military... etc) | **P5.2**, P5.3 |
| Hemp plant | **P2.7** |
| Corn | **P2.7**, P1.4 |
| Bear | **P5.8** |
| Cards | **P5.6** |
| Doors with cards | **P5.7** (+ P4.7, P4.10) |
| Doors with keypad | **P4.8** (+ P4.7) |
| Tools | **P2.3** |
| Clothing | **P3.1**, **P3.2** |
| Food (what's not already in vanilla) | **P1.4** |
| Components | **P2.5** (+ P2.9 recycler) |
| Bandages and Syringe | **P1.8** |
| Effects – radiation → custom biome | **P1.5**, **P5.4** |
| Effects – bleeding → when shot | **P1.6** (+ P7.1 damage type) |
| Effects – freezing → minecraft default | **P1.10** |
| Property to add radiation and cold protection | **P1.7** |
| +Thirst bar (still/move/run/jump, max 12, saturation) | **P1.1** (+ P0.6, P1.9) |
| Guns | **P7.1**, **P7.3**, P7.4 |
| Ammunition | **P7.2** |
| Drinkable water (fluid) | **P1.2** |
| Force gamma to be low | **P1.3** |
| BodiesBodies mod | **P8.1** |
| Hold my items mod | **P8.2** |
| Future: Spawn mechanic (beds or shore) | **P8.4** |

**Added beyond the ToDo:** code review and commit (P0.1), version pin (P0.2), client/server split (P0.3), registry cleanup (P0.5), data attachments (P0.6), config (P0.7), dev tools and tests (P0.8), status HUD (P1.9), comfort/regen (P1.11), ore nodes (P2.2), animal harvesting (P2.4), workbenches (P2.6), furnaces and refinery (P2.8), recycler (P2.9), research (P2.10), placement preview (P4.2), building HP (P4.3), tool cupboard (P4.5), upkeep (P4.6), doors (P4.7), key lock (P4.9), deployables (P4.11), loot spawners (P5.3), monuments (P5.5), roads (P5.9), player shops (P6.3), melee/bows (P7.4), explosives (P7.5), traps (P7.6), sleepers (P8.3), teams (P8.5), map (P8.6), polish and release (Phase 9).

---

## 12. Suggested first sprint (concrete next steps)

1. **P0.1**: commit the local vending work, run client and server, write `STATUS.md`.
2. **P0.2**: pin 1.21.1 + stable Loom, then build.
3. **P0.3**: split source sets and fix the two client-import problems.
4. **P0.4**: write the datagen providers, regenerate, delete the hand-made JSON, add the CI diff check.
5. **P0.5 → P0.6**: registry helpers, then move thirst onto attachments.
6. **P1.1**: finish thirst with the ToDo rules. This is the first "real" feature on the new foundation.

---

### References
- Rust Wiki (Fandom): https://rust.fandom.com/wiki/Rust_Wiki
- Facepunch Rust wiki: https://wiki.facepunch.com/rust
- Components and recycling values: https://xgamingserver.com/blog/rust-components-recycle-guide/
- Monuments, keycards and radiation levels: https://xgamingserver.com/blog/rust-monuments-guide-loot-puzzles-keycards/
- Building grades, TC and decay: https://xgamingserver.com/blog/rust-base-building-guide/
- Fabric docs (datagen, data attachments, networking, split source sets): https://docs.fabricmc.net/
