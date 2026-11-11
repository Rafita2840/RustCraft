# RustCraft – Status

> Living document, updated at the end of each task. Created in **P0.1** (Nov 2026).
> Audited commit: `3cab212` ("Added PLAN.md", `master`).

**Legend:** ✅ works · ⚠️ partial / has known problems · ❌ broken or missing · ⬜ not yet tested in-game

## How this audit was done

- **Static review** of every file in `src/` (code, models, blockstates, lang, textures) against `docs/PLAN.md` §1.2. The "Code" column reflects this.
- **Build / `runClient` / `runServer` / in-game tests** have **not** been run yet. They need a machine with Gradle access, so they are left to the "In-game" column and the checklist in the last section. Every ⬜ turns into ✅/⚠️/❌ once you have tested it.

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
| **Dedicated server** | ❌ | ⬜ | Two common-code files import client classes (see below). Expected to crash on `runServer` | P0.3 |

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
| 19 | `gradlew` is not executable in git (mode 100644) | repo root | P0.2 |
| 20 | `org.gradle.jvmargs=-Xmx1G` is low for Loom + datagen | `gradle.properties` | P0.2 |
| 21 | `fabric.mod.json` still has template values (`homepage` fabricmc.net, `suggests: another-mod`) | `fabric.mod.json` | P0.2 |
| 23 | **Project did not compile** (vending machine used non-existent `readRegistryValue`/`writeRegistryValue`, and a lambda where a `PacketCodec` is required). Fixed by making the block entity an `ExtendedScreenHandlerFactory` with a trade codec. Needs a re-run to confirm | `VendingMachineScreenHandler`, `ModScreenHandlers`, `VendingMachineBlockEntity`, `VendingMachineTrade` | P0.1 (fixed, unverified) |
| 22 | CI only runs `./gradlew build`: no server boot, no datagen check | `.github/workflows/build.yml` | P0.3, P0.4 |

## Manual checklist for P0.1 (needs you)

Run these on your machine and update the "In-game" column above.

**Build and run**
- [ ] `./gradlew build`: note any errors/warnings
- [ ] `./gradlew runClient`: does it reach the title screen? Any errors in the log?
- [ ] `./gradlew runServer` (accept the EULA in `run/eula.txt`): does it boot or crash? Expected: crash with a `net.minecraft.client` / `ClientPlayNetworking` error (problems 1 and 2)

**In-game, creative world** (flat world is easiest)
- [ ] Both RustCraft creative tabs: list which items/blocks are missing, which have purple/black textures
- [ ] Place every block (sulfur ore, buildable grass/sand, all building blocks, drinkable block, crate, vending machine)
- [ ] Crate: open it, move items, break it, reopen
- [ ] Vending machine: place, open, buy something, try with too few items
- [ ] Get blueprint stairs/wall with `/give @s rustcraft:blueprint_wall` (they aren't in the tab) and check they work
- [ ] Floor/wall/stairs: build on a buildable grass block, then off-grid and on a non-buildable surface; check the error messages
- [ ] Hammer: upgrade wood→stone→metal→HQM (survival, with resources); sneak-click a wooden piece to remove it
- [ ] Hazmat suit: wear all 4 pieces (does it render on the body?)
- [ ] Thirst (survival): start value on a **new world** (expected 0), then stand still / walk / sprint / jump / swim for a minute each and watch the bar. Note whether it ever goes down
- [ ] Thirst: die, respawn (expected 10), quit and rejoin
- [ ] Thirst HUD: go underwater (does it overlap the bubbles?) and ride a horse
- [ ] Drinkable block: sneak + right-click it. Does thirst rise?

Send me the results (or just the failures) and I'll update this file and close P0.1.
