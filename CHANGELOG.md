# Changelog for Wicked Hands

## Note

This change log includes the summarized changes. For the full changelog, please go to
the [GitHub History][history] instead.

### 1.0.0

- Fixed companion remains beside a full inventory burning, despawning or being taken by others.
- Fixed placed companion remains being destroyed by mobs, lava or other players.
- Fixed a lifeless hand being destroyed while its ritual is running.
- Fixed a failed revival removing the lifeless hand or waking a second copy of the companion.
- Added wounded companion hands retreating from combat until they are healed to half health.
- Added a look of its own to the ritual of every hand, from snowflakes to witch sparkles.

### 0.5.0 (beta)

- Fixed shoulder hands falling off on damage their bonus protects against, like fire or poison
  arrows.
- Fixed the "Drowned Hand" falling off in water and the "Stray Hand" in powder snow.
- Changed companion hands to ignore Creepers, they no longer join a fight against them.
- Changed the required version to Easy NPC 7.17.1.
- Added healing a companion hand by feeding it its ritual offering, one heart per item.
- Added slow regeneration for companion hands while they are out of combat.
- Added companion hands ignoring the pets and companions of other players.
- Added the `wicked_hands:companion_ignored_targets` tag for mobs that companions leave alone.
- Added the "Bogged" and "Witch Hand", rare drops of Bogged and Witches killed by a player.
- Added Mushrooms and Spider Eye as their ritual offerings, plus spawn eggs and advancements.
- Added their hits poisoning (Bogged) or cursing with Poison, Slowness or Weakness (Witch).
- Added poison immunity to the "Bogged Hand".
- Added the "Witch Hand" mending its badly wounded owner and their hands with Regeneration.
- Added shoulder bonuses curing Poison (Bogged) and granting Night Vision (Witch).

### 0.4.0 (beta)

- Added the "Husk", "Stray" and "Withered Hand", rare drops of Husks, Strays and Wither Skeletons.
- Added Sand, Snowball and Coal as their ritual offerings, plus spawn eggs and advancements.
- Added their hits weakening (Husk), slowing and chilling (Stray) or withering (Withered).
- Added fire immunity to the "Withered Hand".
- Added a bonus for every hand riding on its owner's shoulder, shown as an effect with a hand icon.

### 0.3.0 (beta)

- Fixed revived hands staying listed as a saved NPC in Easy NPC after the ritual.
- Fixed the mod version shown on NeoForge.
- Changed fallen companion hands to leave only their remains instead of their usual loot.
- Changed the required versions to Easy NPC 7.16.0 and Easy Model Entities 2.6.0.
- Changed switching a companion's mode with Sneak + Right-Click to require an empty hand.
- Changed the "By Candlelight" advancement, earlier progress for it starts over.
- Changed the lifeless hands to whisper their revival hint in chat, with details in the tooltip.
- Added a "grab" to companion hands, a hit holds the target in place and slows it for a moment.
- Added the remains of a fallen companion returning to its owner's inventory.
- Added protected remains at the place of death when the owner is offline or dead.
- Added owner-bound remains, only their owner can revive them or pick up protected remains.
- Added reviving the remains with the ritual, waking the same companion with its name.
- Added the placed "Lifeless Rotten Hand" lying on its back.
- Added the "Lifeless Drowned Hand", rarely dropped by Drowned killed by a player.
- Added the "Lifeless Bone Hand", rarely dropped by Skeletons killed by a player.
- Added Kelp as the ritual offering for the "Drowned Hand" and Bone Meal for the "Bone Hand".
- Added diving to the "Drowned Hand", it breathes underwater and swims fast.
- Added the "Drowned Hand" putting out its owner when the owner is on fire.
- Added the "Bone Hand" pointing at monsters that approach or hunt its owner.
- Added a wake-up animation and a wave at the owner when a companion is revived.
- Added companions waving at other players who come close.
- Added hurt and sitting animations to every hand.
- Added spawn eggs and advancements for the "Drowned Hand" and the "Bone Hand".
- Added "Shoulder" after "Follow", the companion rides on its owner's shoulder like a parrot.
- Added companion hands joining in when their owner attacks something.
- Added companions in "Follow" mode joining their owner in other dimensions.

### 0.2.0 (alpha)

- Changed the revival hint of the "Lifeless Rotten Hand" to describe the new ritual.
- Added placing the pale, motionless "Lifeless Rotten Hand" on Soul Sand or Soul Soil.
- Added a revival ritual: 4 burning candles and Rotten Flesh wake a placed hand as your companion.
- Added soul flames, whispers and a heartbeat for every candle lit around a placed hand.
- Added a soul vortex, a darkening storm and a lightning strike to the 7-second revival ritual.
- Added a short Darkness effect for players near a starting revival ritual.
- Added companion hands that follow their owner and attack whoever hurts the owner.
- Added "Follow" and "Stay" for companion hands, toggled by the owner with Sneak + Right-Click.
- Added swimming to the "Rotten Hand", companions follow their owner through water.
- Added advancements for picking up a hand, starting the ritual and waking a companion.

### 0.1.0 (alpha)

- Added Easy NPC and Easy Model Entities as required dependencies.
- Added the "Rotten Hand", a rare mob that wanders the Overworld at night.
- Added the "Lifeless Rotten Hand" item, dropped by every "Rotten Hand" and rarely by zombies.
- Added attacks to the "Rotten Hand", it goes after players and animals for light damage.
- Added alternating idle and attack animations to the "Rotten Hand".
- Added a hint about reviving the "Lifeless Rotten Hand" when the item is used.
- Added a "Rotten Hand Spawn Egg", also listed in the vanilla "Spawn Eggs" tab.
- Added the "Wicked Hands" creative tab with all items of the mod.

[history]: https://github.com/MarkusBordihn/BOs-Wicked-Hands/commits/
