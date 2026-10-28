/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.wickedhands.gametest;

import de.markusbordihn.easynpc.api.handler.EasyNPCShoulderHandler;
import de.markusbordihn.easynpc.data.npc.ShoulderSide;
import de.markusbordihn.wickedhands.entity.AbstractHandEntity;
import de.markusbordihn.wickedhands.entity.BoggedHandEntity;
import de.markusbordihn.wickedhands.entity.BoneHandEntity;
import de.markusbordihn.wickedhands.entity.DrownedHandEntity;
import de.markusbordihn.wickedhands.entity.HuskHandEntity;
import de.markusbordihn.wickedhands.entity.RottenHandEntity;
import de.markusbordihn.wickedhands.entity.ShoulderPassive;
import de.markusbordihn.wickedhands.entity.ShoulderPassiveHandler;
import de.markusbordihn.wickedhands.entity.StrayHandEntity;
import de.markusbordihn.wickedhands.entity.WitchHandEntity;
import de.markusbordihn.wickedhands.entity.WitheredHandEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ShoulderGameTests extends HandGameTests {

  private static final int OWNER_HUNGER_TICKS = 200;
  private static final int OWNER_BURNING_TICKS = 200;
  private static final int PASSIVE_MEASURE_TICKS = 40;
  // Player.removeEntitiesOnShoulder ignores the first 20 ticks after a hand sat down.
  private static final int AFTER_SHOULDER_GRACE_TICKS = 25;
  // ServerPlayer ignores most damage during the first 60 ticks after joining.
  private static final int AFTER_SPAWN_INVULNERABILITY_TICKS = 65;

  private static ServerPlayer makeVulnerableOwner(GameTestHelper helper) {
    ServerPlayer owner = makeServerPlayer(helper);
    owner.getAbilities().invulnerable = false;
    placeBesideRitual(helper, owner);
    return owner;
  }

  private static ServerPlayer sitBesideRottenHand(GameTestHelper helper, ResourceLocation handId) {
    ServerPlayer owner = makeVulnerableOwner(helper);
    sitOnShoulder(helper, owner, handId);
    sitOnShoulder(helper, owner, RottenHandEntity.ENTITY_ID);
    return owner;
  }

  private static boolean isOnShoulder(
      ServerPlayer owner, ShoulderSide side, ResourceLocation handId) {
    return EasyNPCShoulderHandler.getShoulderNPCData(owner, side)
        .map(entityTag -> entityTag.getString("id"))
        .map(ResourceLocation::tryParse)
        .filter(handId::equals)
        .isPresent();
  }

  private static void assertOnlyGrippingHandStayed(
      GameTestHelper helper, ServerPlayer owner, ResourceLocation handId, String cause) {
    helper.assertTrue(
        isOnShoulder(owner, ShoulderSide.LEFT, handId),
        handId + " was thrown off the shoulder by " + cause);
    helper.assertFalse(
        isOnShoulder(owner, ShoulderSide.RIGHT, RottenHandEntity.ENTITY_ID),
        "The Rotten hand held on through " + cause);
  }

  private static void sitOnShoulder(
      GameTestHelper helper, ServerPlayer owner, ResourceLocation handId) {
    AbstractHandEntity hand = spawnWildHand(helper, handId, TEST_POSITION);
    // Player.setEntityOnShoulder rejects players that are not on the ground.
    owner.setOnGround(true);
    helper.assertTrue(
        EasyNPCShoulderHandler.sitOnShoulder(hand, owner),
        "Hand could not sit on the shoulder");
  }

  private static boolean hasShoulderPassive(ServerPlayer owner, ShoulderPassive shoulderPassive) {
    return owner.hasEffect(shoulderPassive.getEffect())
        && shoulderPassive
            .getAttribute()
            .map(
                attribute ->
                    owner.getAttribute(attribute).getModifier(shoulderPassive.getModifierUUID())
                        != null)
            .orElse(true);
  }

  private static void assertShoulderHandsGrantPassives(
      GameTestHelper helper,
      ResourceLocation leftHandId,
      ShoulderPassive leftPassive,
      ResourceLocation rightHandId,
      ShoulderPassive rightPassive) {
    ServerPlayer owner = makeServerPlayer(helper);
    sitOnShoulder(helper, owner, leftHandId);
    sitOnShoulder(helper, owner, rightHandId);
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        hasShoulderPassive(owner, leftPassive) && hasShoulderPassive(owner, rightPassive),
        "Hands on the shoulder did not grant their passives");

    owner.removeAllEffects();
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        hasShoulderPassive(owner, leftPassive) && hasShoulderPassive(owner, rightPassive),
        "Passives did not return after drinking milk");

    EasyNPCShoulderHandler.dismount(owner, ShoulderSide.LEFT);
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        !hasShoulderPassive(owner, leftPassive) && hasShoulderPassive(owner, rightPassive),
        "Dismounting one hand did not remove only its passive");

    EasyNPCShoulderHandler.dismount(owner, ShoulderSide.RIGHT);
    ShoulderPassiveHandler.update(owner);
    helper.assertFalse(
        hasShoulderPassive(owner, rightPassive), "Passive stayed after the hand left the shoulder");
    helper.succeed();
  }

  public void testShoulderBoneAndWitheredHandsGrantPassives(GameTestHelper helper) {
    assertShoulderHandsGrantPassives(
        helper,
        BoneHandEntity.ENTITY_ID,
        ShoulderPassive.BONE_GUARD,
        WitheredHandEntity.ENTITY_ID,
        ShoulderPassive.ASHEN_SKIN);
  }

  public void testShoulderRottenAndDrownedHandsGrantPassives(GameTestHelper helper) {
    assertShoulderHandsGrantPassives(
        helper,
        RottenHandEntity.ENTITY_ID,
        ShoulderPassive.FIRM_GRIP,
        DrownedHandEntity.ENTITY_ID,
        ShoulderPassive.SECOND_BREATH);
  }

  public void testShoulderWitheredHandShortensBurning(GameTestHelper helper) {
    ServerPlayer owner = makeVulnerableOwner(helper);
    sitOnShoulder(helper, owner, WitheredHandEntity.ENTITY_ID);
    ShoulderPassiveHandler.update(owner);
    owner.setRemainingFireTicks(OWNER_BURNING_TICKS);
    helper.onEachTick(owner::doTick);
    helper.runAfterDelay(
        PASSIVE_MEASURE_TICKS,
        () -> {
          helper.assertTrue(
              owner.getRemainingFireTicks()
                  <= OWNER_BURNING_TICKS - PASSIVE_MEASURE_TICKS * 3 / 2,
              "Withered hand on the shoulder did not shorten the burning");
          helper.succeed();
        });
  }

  public void testShoulderDrownedHandSlowsDrowning(GameTestHelper helper) {
    ServerPlayer owner = makeVulnerableOwner(helper);
    sitOnShoulder(helper, owner, DrownedHandEntity.ENTITY_ID);
    helper.setBlock(TEST_POSITION, Blocks.WATER);
    helper.setBlock(TEST_POSITION.above(), Blocks.WATER);
    owner.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION)));
    ShoulderPassiveHandler.update(owner);
    helper.onEachTick(owner::doTick);
    helper.runAfterDelay(
        PASSIVE_MEASURE_TICKS,
        () -> {
          int lostAir = owner.getMaxAirSupply() - owner.getAirSupply();
          helper.assertTrue(
              lostAir > 0 && lostAir < PASSIVE_MEASURE_TICKS * 3 / 4,
              "Drowned hand on the shoulder did not slow down drowning, lost " + lostAir + " air");
          helper.succeed();
        });
  }

  public void testShoulderPassiveAppliesOnPlayerTick(GameTestHelper helper) {
    ServerPlayer owner = makeServerPlayer(helper);
    sitOnShoulder(helper, owner, BoneHandEntity.ENTITY_ID);
    // The mock player has no ticking connection, so NeoForge and Forge never fire its tick event.
    helper.onEachTick(owner::doTick);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                hasShoulderPassive(owner, ShoulderPassive.BONE_GUARD),
                "The player tick did not apply the shoulder passive"));
  }

  public void testShoulderHandsProtectFromHungerAndFreezing(GameTestHelper helper) {
    ServerPlayer owner = makeServerPlayer(helper);
    sitOnShoulder(helper, owner, HuskHandEntity.ENTITY_ID);
    sitOnShoulder(helper, owner, StrayHandEntity.ENTITY_ID);
    owner.addEffect(new MobEffectInstance(MobEffects.HUNGER, OWNER_HUNGER_TICKS));
    owner.setTicksFrozen(owner.getTicksRequiredToFreeze());
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        owner.hasEffect(ShoulderPassive.DRY_STOMACH.getEffect())
            && owner.hasEffect(ShoulderPassive.COLD_BLOOD.getEffect()),
        "Husk and Stray hands did not show their passive effects");
    helper.assertFalse(
        owner.hasEffect(MobEffects.HUNGER),
        "Husk hand on the shoulder did not remove hunger");
    helper.assertTrue(
        owner.getTicksFrozen() == 0,
        "Stray hand on the shoulder did not stop freezing");
    helper.succeed();
  }

  public void testShoulderHandsCurePoisonAndGrantNightVision(GameTestHelper helper) {
    ServerPlayer owner = makeServerPlayer(helper);
    sitOnShoulder(helper, owner, BoggedHandEntity.ENTITY_ID);
    sitOnShoulder(helper, owner, WitchHandEntity.ENTITY_ID);
    owner.addEffect(new MobEffectInstance(MobEffects.POISON, OWNER_POISON_TICKS));
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        owner.hasEffect(ShoulderPassive.MOSSY_VEINS.getEffect())
            && owner.hasEffect(ShoulderPassive.WITCH_SIGHT.getEffect()),
        "Bogged and Witch hands did not show their passive effects");
    helper.assertFalse(
        owner.hasEffect(MobEffects.POISON),
        "Bogged hand on the shoulder did not remove poison");
    helper.assertTrue(
        owner.hasEffect(MobEffects.NIGHT_VISION),
        "Witch hand on the shoulder did not grant night vision");
    helper.succeed();
  }

  public void testShoulderHandsHoldOnThroughTheirDamage(GameTestHelper helper) {
    Map<ResourceLocation, Function<DamageSources, DamageSource>> grippingDamage =
        Map.of(
            WitheredHandEntity.ENTITY_ID, DamageSources::onFire,
            StrayHandEntity.ENTITY_ID, DamageSources::freeze,
            BoggedHandEntity.ENTITY_ID, DamageSources::magic,
            DrownedHandEntity.ENTITY_ID, DamageSources::drown,
            HuskHandEntity.ENTITY_ID, DamageSources::starve);
    Map<ResourceLocation, ServerPlayer> owners = new HashMap<>();
    grippingDamage
        .keySet()
        .forEach(handId -> owners.put(handId, sitBesideRottenHand(helper, handId)));
    helper.runAfterDelay(
        AFTER_SPAWN_INVULNERABILITY_TICKS,
        () -> {
          grippingDamage.forEach(
              (handId, damage) -> {
                ServerPlayer owner = owners.get(handId);
                DamageSource damageSource = damage.apply(owner.damageSources());
                owner.hurt(damageSource, 1.0F);
                assertOnlyGrippingHandStayed(
                    helper, owner, handId, damageSource.getMsgId() + " damage");

                owner.hurt(owner.damageSources().generic(), 1.0F);
                helper.assertFalse(
                    isOnShoulder(owner, ShoulderSide.LEFT, handId),
                    handId + " held on through generic damage");
              });
          helper.succeed();
        });
  }

  public void testShoulderHandsStayInTheirEnvironment(GameTestHelper helper) {
    ServerPlayer drownedOwner = sitBesideRottenHand(helper, DrownedHandEntity.ENTITY_ID);
    ServerPlayer strayOwner = sitBesideRottenHand(helper, StrayHandEntity.ENTITY_ID);
    helper.runAfterDelay(
        AFTER_SHOULDER_GRACE_TICKS,
        () -> {
          helper.setBlock(TEST_POSITION, Blocks.WATER);
          drownedOwner.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION)));
          drownedOwner.doTick();
          helper.assertTrue(
              drownedOwner.isInWater(), "The owner did not enter the water");
          assertOnlyGrippingHandStayed(helper, drownedOwner, DrownedHandEntity.ENTITY_ID, "water");

          // Entity.baseTick resets isInPowderSnow before Player.aiStep checks the shoulders.
          strayOwner.setIsInPowderSnow(true);
          strayOwner.aiStep();
          assertOnlyGrippingHandStayed(
              helper, strayOwner, StrayHandEntity.ENTITY_ID, "powder snow");
          helper.succeed();
        });
  }
}
