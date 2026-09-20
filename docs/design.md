# Design

## Design

**The mod decides one thing: where.** Placing a block normally needs something to
place it against, because the position comes from the face the crosshair is on. With
nothing under the crosshair there is no face, so no position, so no placement. That
missing coordinate is the whole of what stands between a player and a walkway over
open air — there is no rule about support to get around.

So `AngelBlockItem.use` supplies the coordinate and hands the rest back, through
`ItemStack.useOn`. Everything after that is vanilla's own placement path: whether the
space is free, whether a claim mod objects, the sound, the shrunk stack, the check
that stops you putting a block inside your own head. None of it is reimplemented,
because a second copy is only a second thing to keep in step — and the second copy is
always the one that forgets to fire the event a protection mod is listening for.

That delegation is worth more than it sounds. Asking `canBeReplaced` rather than
`== Blocks.AIR` is what lets the block go into water, tall grass and snow; going
through `CommonHooks.onPlaceItemIntoWorld` is what lets a claim mod cancel it and get
the world restored. Both come free.

**The distance is not a number.** It is `Player.blockInteractionRange()` — the
attribute that already decides how far away an ordinary block can be placed, 4.5 in a
vanilla game. Reading it means the two can never disagree, and that anything which
extends a player's reach extends this with it, without this mod knowing such a thing
exists. There is no config, because there is nothing left to put in one.

**Straight back to the hand, and only once.** `AngelBlock.onDestroyedByPlayer` puts
the block in the player's inventory, and the block declares `noLootTable()` so nothing
is dropped on the floor. The two go together: a self-drop table would hand the block
back twice. The cost is that anything destroying it some other way — an explosion,
another mod calling `destroyBlock` — loses it outright, which is the same bargain the
block already makes by breaking instantly.

**Being see-through takes three things, and they fail differently.** Alpha in the
sprite, `cutout` on the model, `noOcclusion()` on the block. Miss the first and the
holes render as opaque black; miss the second and the same; miss the third and the
faces behind the holes are culled away, so the gaps show whatever is drawn past the
world. Only the third is reachable from a test, and it has one. The other two are
checked where they are made — `tools/make_textures.py` writes RGBA and the model
provider writes the render type.


## Prior art

The mechanic is Extra Utilities', and has been reimplemented many times since. What is
here was written against 1.21.1 from scratch, but not in ignorance:
[AngelBlockRenewed](https://github.com/LaidBackSloth/AngelBlockRenewed) (MIT) was read
first, and the recipe — four feathers at the corners, four sticks between them — is
kept because it is the one every version of this block has used and there is nothing
to gain by making players look up a new one.

What is different from that version: the reach is the attribute rather than a literal
4.5; placement goes through `ItemStack.useOn` rather than a bare `setBlock`, so
protection mods and the placement sound and the client's own prediction all work; and
the sprite is generated rather than drawn.

**Why not just use [Angel Extra Utilities](https://www.curseforge.com/minecraft/mc-mods/angel-utilities)**,
which already ships an angel block for 1.21.1 NeoForge. Its jar was disassembled
before this repository was started. Its block is opaque — the texture is a palette PNG
with no `tRNS` chunk at all, so there is no transparency in the file to render — and
it overrides `getLightBlock` to 15, blocking all light. Its placement is
`BlockPos.containing(x + 1, y + 2, z)` off the player's own position: a fixed spot one
block east and two up, with no reading of the look direction anywhere in the class. It
requires the target to be exactly `Blocks.AIR`, so it will not go into water. It has
`strength(1.0, 5.0)` rather than breaking instantly, and no return-to-inventory at
all. It is a different block that shares a name.

## Roadmap

- [x] **0** — scaffold, registry, creative tab, datagen, sprite
- [x] **1** — the block: mid-air placement, water, return to hand, seven game tests
- [ ] **2** — looked at in a client. Whether the sprite reads at a distance and whether
  the hole is the right size are the two things no test can answer
- [ ] **3** — the licence

## License

MIT. Decided on 2026-08-29.

MIT is the choice that puts the fewest obstacles in front of a modpack: All Rights
Reserved would have meant pack authors quietly leaving it out. It also matches the
rest of the set, so nobody has to check which of them is which.
