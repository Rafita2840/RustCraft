# RustCraft – Status

> Living document, updated at the end of each task. Created in **P0.1** (Nov 2026).
> Audited commit: `3cab212` ("Added PLAN.md", `master`).

**Legend:** ✅ works · ⚠️ partial / has known problems · ❌ broken or missing · ⬜ not yet tested in-game

## How this audit was done

- **Static review** of every file in `src/` (code, models, blockstates, lang, textures) against `docs/PLAN.md` §1.2. The "Code" column reflects this.
- **Build, `runClient`, `runServer` and in-game tests** were run by the project owner. Results are in the "In-game" column; ⬜ means not tested yet.

## Git state (P0.1 step 1)

| Check | Result |
|---|---|
| Vending-machine rewrite pushed | ✅ `VendingMachine*` classes, `VendingTradePacket`, `VendingMachineScreen(Handler)` are on `master` |
| Old `ResourceVendingMachine*` classes removed | ✅ none left in `src/` |
| Leftover naming from the old version | ⚠️ the block models still point at `resource_vending_machine_block*` textures → P0.4 |
| `gradlew` executable bit | ⚠️ stored as mode `100644`; CI does `chmod +x`, local Windows is fine → P0.2 |

## Features

| Feature | Code | In-game | Notes | Task |
|---|---|---|---|---|
| **Resource items** (scrap, cloth, low grade fuel, sulfur, animal fat, stone, wood, metal, HQM) | ✅ | ⬜ | Registered, model, texture and lang all present. No source for any of them yet except `sulfur_ore` | P2.1 |
| **Sulfur ore** | ✅ | ⬜ | Block, loot table and mineable/needs-stone tags exist. No worldgen, so it never spawns naturally | P2.1 |
| **Low grade fuel recipe** | ✅ | ⬜ | Only recipe in the mod, hand-written JSON | P0.4 |
| **Hammer** | ⚠️ | ⬜ | Upgrade twig→stone→metal→HQM and sneak-remove exist. Imports `client.gui.screen.Screen` in common code. Resource consumption is duplicated and fragile | P0.3, P4.1 |
| **Blueprint – floor** | ⚠️ | ⬜ | 3×3 floor on the 3-block grid. Same `Screen` import problem | P0.3, P4.1 |
| **Blueprint – wall** | ⚠️ | ⬜ | Works per code, but is **missing from the creative tab** | P0.5 |
| **Blueprint – stairs** | ⚠️ | ⬜ | Places **vanilla oak stairs**, not `wooden_building_stairs`. Missing from the creative tab | P0.4, P0.5 |
| **Building blocks** (wood/stone/metal/HQM + centers) | ⚠️ | ⬜ | All `strength(-1)`: unbreakable, but explosion resistance also drops to 0 | P4.3 |
| **`wooden_building_stairs`** | ❌ | – | Registered but has no blockstate, model, item model or lang key, and nothing uses it. Remove it or finish it | P0.4 |
| **Buildable grass / sand blocks** | ✅ | ⬜ | Blockstate, model, texture, lang present | – |
| **Hazmat suit** (4 pieces) | ❌ | ⬜ | Item models reference `item/hazmat_suit_*` but **no item textures exist** (purple/black in inventory). Armor layer textures exist. No protection stat yet | P0.4, P1.7 |
| **Drinkable block** (debug) | ⚠️ | ⬜ | Model references `block/drinkable_block` but **no texture exists**. To be removed or made creative-only | P0.4, P1.1 |
| **Crate** (9 slots) | ⚠️ | ⬜ | Block entity and screen exist. No loot table, no refill | P5.1 |
| **Vending machine** | ⚠️ | ⬜ | Block (2-tall, facing), block entity, trades, screen, C2S trade packet. `VendingTradePacket` imports client networking in common code | P0.3, P6.1 |
| **Thirst – storage / sync** | ⚠️ | ⬜ | Works through a mixin that adds NBT to every entity. Re-synced on join and respawn | P0.6 |
| **Thirst – drain logic** | ❌ | ⬜ | Speed thresholds don't match real values; jumping isn't detected; capped at 5000 then −1 thirst | P1.1 |
| **Thirst – consequences** | ❌ | – | Thirst 0 does nothing | P1.1 |
| **Thirst – starting value** | ❌ | ⬜ | New players start at **0**, respawn gives **10**, max is **20** | P1.1 |
| **Thirst – HUD** | ⚠️ | ⬜ | Draws 10 droplets at `y − 53`, which overlaps air bubbles and mount health | P1.1 |
| **Thirst – networking** | ⚠️ | – | A saturation packet is sent **every tick per player**; the client never uses it | P0.6 |
| **Creative tabs** | ⚠️ | ⬜ | 2 tabs, filled by hand; some items missing | P0.5 |
| **DataGen** | ❌ | – | `RustCraftDataGenerator` is empty; everything is hand-written JSON | P0.4 |
| **Dedicated server** | ✅ | ✅ `runServer` boots | Client imports were removed from common code in P0.3 (CI boot step still to be added by hand) | P0.3 |

## Problems (PLAN §1.2) – verified against the code

| # | Problem | Confirmed? | Fixed in |
|---|---|---|---|
| 1 | `VendingTradePacket` imports `ClientPlayNetworking` (common class) | ✅ line 4 and `send()` at line 69 | P0.3 |
| 2 | `HammerItem` / `BlueprintFloorItem` import `net.minecraft.client.gui.screen.Screen` | ✅ (`BlueprintWallItem` and `BlueprintStairsItem` do **not**) | P0.3 |
| 3 | Thirst starts at 0, respawn sets 10, max 20 | ✅ `RustCraft` join handler reads default 0; `ServerPlayerEntityMixin` sets 10 | P1.1 |
| 4 | Saturation packet every tick | ✅ `addSaturation` always calls `syncSaturation` | P0.6 / P1.1 |
| 5 | Speed thresholds don't match real speeds | ⚠️ code matches the description (`> 0`, `> 0.13`, `> 0.20`); actual values still to be measured in-game | P1.1 |
| 6 | Thirst 0 does nothing; only debug block gives water | ✅ | P1.1, P1.2 |
| 7 | HUD overlaps air bubbles / mount health | ✅ hard-coded `y − 53`, no checks | P1.1 |
| 8 | `strength(-1)` → zero explosion resistance | ✅ all building blocks (and the stairs) | P4.3 |
| 9 | `wooden_building_stairs` has no assets; blueprints missing from tab | ✅ stairs and wall blueprints missing from `ModGroups` | P0.4 / P0.5 |
| 10 | `IEntityDataSaver` mixin on every entity | ✅ `ModEntityDataSaverMixin` | P0.6 |
| 11 | Duplicated inventory-consumption code | ⬜ not reviewed in depth | P4.1 |
| 12 | `§` codes in lang | ✅ all text/tooltip strings | P0.4 |
| 13 | Unused `ModMessages` | ✅ no references outside the file | P0.5 |
| 14 | Local work not pushed | ✅ resolved, the vending rewrite is on `master` | P0.1 |

### New problems found during this audit

| # | Problem | Where | Fixed in |
|---|---|---|---|
| 15 | Hazmat suit item textures are missing (4 files) | `textures/item/hazmat_suit_*.png` | P0.4 |
| 16 | `drinkable_block` texture is missing | `textures/block/drinkable_block.png` | P0.4 |
| 17 | `wooden_building_stairs` is registered but unused (blueprint places `OAK_STAIRS`) | `ModBlocks`, `BlueprintStairsItem` | P0.4 |
| 18 | Vending machine models/textures still use the old `resource_vending_machine_block*` names | `models/block/vending_machine*.json` | P0.4 |
| 19 | `gradlew` is not executable in git (mode 100644) | repo root | P0.2 (needs the manual chmod below) |
| 20 | `org.gradle.jvmargs=-Xmx1G` is low for Loom + datagen | `gradle.properties` | P0.2 ✅ (see below) |
| 21 | `fabric.mod.json` still has template values (`homepage` fabricmc.net, `suggests: another-mod`) | `fabric.mod.json` | P0.2 ✅ (see below) |
| 23 | **Project did not compile** (vending machine used non-existent `readRegistryValue`/`writeRegistryValue`, and a lambda where a `PacketCodec` is required). Fixed by making the block entity an `ExtendedScreenHandlerFactory` with a trade codec. Needs a re-run to confirm | `VendingMachineScreenHandler`, `ModScreenHandlers`, `VendingMachineBlockEntity`, `VendingMachineTrade` | P0.1 (fixed, unverified) |
| 22 | CI only runs `./gradlew build`: no server boot, no datagen check | `.github/workflows/build.yml` | P0.3, P0.4 |

## P0.2 – Version pin (Nov 2026)

| Setting | Before | After |
|---|---|---|
| Minecraft | `1.21` | `1.21.1` |
| Yarn | `1.21+build.9` | `1.21.1+build.3` |
| Fabric API | `0.102.0+1.21` | `0.107.0+1.21.1` (can be bumped to the newest `+1.21.1` build) |
| Loom | `1.11-SNAPSHOT` | `1.11.8` (stable) |
| `fabric.mod.json` | `~1.21`, template `suggests`, fabricmc.net homepage | `~1.21.1`, `suggests` removed, GitHub homepage |
| Gradle memory | `-Xmx1G` | `-Xmx2G` |

Still to do by hand: `git update-index --chmod=+x gradlew` (problem #19). Build, `runClient` and `runServer`: ✅.

## P0.3 – Client/server split (Nov 2026)

- `build.gradle`: Loom `splitEnvironmentSourceSets()` plus the `mods { rustcraft { ... } }` block.
- Moved to `src/client/java` (same packages): `RustCraftClient`, `client/ThirstHudOverlay`, `screen/custom/DefaultCrateScreen`, `screen/custom/VendingMachineScreen`.
- New `client/network/ClientPackets` holds the client-side vending trade sender; `VendingTradePacket.send()` is removed (problem #1).
- New common `util/TooltipKeys`: tooltips ask it for Shift/Control; `RustCraftClient` plugs in the real key checks, a dedicated server always gets `false` (problem #2).
- CI (`build.yml`) now also boots a dedicated server and fails if it never reports `Done` (problem #22).
- Result: ✅ `./gradlew build`, `runClient` and `runServer` all work. Pending: replace `.github/workflows/build.yml` by hand (protected file) so CI runs the server boot step.

## P0.1 – Result

Done. Build, `runClient` and `runServer` run, the creative-mode checks were done in-game, and every problem has a task ID. Anything the checks did not cover stays ⬜ in the table above (for example the exact thirst drain and respawn values), to be measured when P1.1 starts.
