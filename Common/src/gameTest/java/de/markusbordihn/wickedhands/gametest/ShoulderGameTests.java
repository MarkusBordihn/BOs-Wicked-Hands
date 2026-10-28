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
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ShoulderGameTests extends HandGameTests {

  private static final int OWNER_HUNGER_TICKS = 200;
  // ServerPlayer.removeEntitiesOnShoulder ignores the first 20 ticks after a hand sat down.
  private static final int AFTER_SHOULDER_GRACE_TICKS = 25;

  private static ServerPlayer makeOwnerInTestArea(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    // TestInstanceBlockEntity.encaseStructure puts a barrier at TEST_POSITION, hands cannot get off.
    owner.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION.above())));
    return owner;
  }

  private static ServerPlayer makeVulnerableOwner(GameTestHelper helper) {
    ServerPlayer owner = makeOwnerInTestArea(helper);
    owner.getAbilities().invulnerable = false;
    // ServerPlayer.isInvulnerableTo spares every player whose client has not loaded yet.
    owner.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
    return owner;
  }

  private static ServerPlayer sitBesideRottenHand(GameTestHelper helper, Identifier handId) {
    ServerPlayer owner = makeVulnerableOwner(helper);
    sitOnShoulder(helper, owner, handId);
    sitOnShoulder(helper, owner, RottenHandEntity.ENTITY_ID);
    return owner;
  }

  private static boolean isOnShoulder(ServerPlayer owner, ShoulderSide side, Identifier handId) {
    return EasyNPCShoulderHandler.getShoulderNPCData(owner, side)
        .flatMap(entityTag -> entityTag.getString("id"))
        .map(Identifier::tryParse)
        .filter(handId::equals)
        .isPresent();
  }

  private static void assertOnlyGrippingHandStayed(
      GameTestHelper helper, ServerPlayer owner, Identifier handId, String cause) {
    helper.assertTrue(
        isOnShoulder(owner, ShoulderSide.LEFT, handId),
        Component.literal(handId + " was thrown off the shoulder by " + cause));
    helper.assertFalse(
        isOnShoulder(owner, ShoulderSide.RIGHT, RottenHandEntity.ENTITY_ID),
        Component.literal("The Rotten hand held on through " + cause));
  }

  private static void sitOnShoulder(GameTestHelper helper, ServerPlayer owner, Identifier handId) {
    AbstractHandEntity hand = spawnWildHand(helper, handId, TEST_POSITION);
    // Player.setEntityOnShoulder rejects players that are not on the ground.
    owner.setOnGround(true);
    helper.assertTrue(
        EasyNPCShoulderHandler.sitOnShoulder(hand, owner),
        Component.literal("Hand could not sit on the shoulder"));
  }

  private static boolean hasShoulderPassive(
      ServerPlayer owner, Holder<Attribute> attribute, ShoulderPassive shoulderPassive) {
    return owner.hasEffect(shoulderPassive.getEffect())
        && owner.getAttribute(attribute).hasModifier(shoulderPassive.getModifierId());
  }

  private static void assertShoulderHandsGrantAttributePassives(
      GameTestHelper helper,
      Identifier leftHandId,
      Holder<Attribute> leftAttribute,
      ShoulderPassive leftPassive,
      Identifier rightHandId,
      Holder<Attribute> rightAttribute,
      ShoulderPassive rightPassive) {
    ServerPlayer owner = makeOwnerInTestArea(helper);
    sitOnShoulder(helper, owner, leftHandId);
    sitOnShoulder(helper, owner, rightHandId);
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        hasShoulderPassive(owner, leftAttribute, leftPassive)
            && hasShoulderPassive(owner, rightAttribute, rightPassive),
        Component.literal("Hands on the shoulder did not grant their passives"));

    owner.removeAllEffects();
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        hasShoulderPassive(owner, leftAttribute, leftPassive)
            && hasShoulderPassive(owner, rightAttribute, rightPassive),
        Component.literal("Passives did not return after drinking milk"));

    EasyNPCShoulderHandler.dismount(owner, ShoulderSide.LEFT);
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        !hasShoulderPassive(owner, leftAttribute, leftPassive)
            && hasShoulderPassive(owner, rightAttribute, rightPassive),
        Component.literal("Dismounting one hand did not remove only its passive"));

    EasyNPCShoulderHandler.dismount(owner, ShoulderSide.RIGHT);
    ShoulderPassiveHandler.update(owner);
    helper.assertFalse(
        hasShoulderPassive(owner, rightAttribute, rightPassive),
        Component.literal("Passive stayed after the hand left the shoulder"));
    helper.succeed();
  }

  public void testShoulderBoneAndWitheredHandsGrantPassives(GameTestHelper helper) {
    assertShoulderHandsGrantAttributePassives(
        helper,
        BoneHandEntity.ENTITY_ID,
        Attributes.ARMOR,
        ShoulderPassive.BONE_GUARD,
        WitheredHandEntity.ENTITY_ID,
        Attributes.BURNING_TIME,
        ShoulderPassive.ASHEN_SKIN);
  }

  public void testShoulderRottenAndDrownedHandsGrantPassives(GameTestHelper helper) {
    assertShoulderHandsGrantAttributePassives(
        helper,
        RottenHandEntity.ENTITY_ID,
        Attributes.KNOCKBACK_RESISTANCE,
        ShoulderPassive.FIRM_GRIP,
        DrownedHandEntity.ENTITY_ID,
        Attributes.OXYGEN_BONUS,
        ShoulderPassive.SECOND_BREATH);
  }

  public void testShoulderPassiveAppliesOnPlayerTick(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    sitOnShoulder(helper, owner, BoneHandEntity.ENTITY_ID);
    // The mock player has no ticking connection, so NeoForge and Forge never fire its tick event.
    helper.onEachTick(owner::doTick);
    helper.succeedWhen(
        () ->
            helper.assertTrue(
                hasShoulderPassive(owner, Attributes.ARMOR, ShoulderPassive.BONE_GUARD),
                Component.literal("The player tick did not apply the shoulder passive")));
  }

  public void testShoulderHandsProtectFromHungerAndFreezing(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    sitOnShoulder(helper, owner, HuskHandEntity.ENTITY_ID);
    sitOnShoulder(helper, owner, StrayHandEntity.ENTITY_ID);
    owner.addEffect(new MobEffectInstance(MobEffects.HUNGER, OWNER_HUNGER_TICKS));
    owner.setTicksFrozen(owner.getTicksRequiredToFreeze());
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        owner.hasEffect(ShoulderPassive.DRY_STOMACH.getEffect())
            && owner.hasEffect(ShoulderPassive.COLD_BLOOD.getEffect()),
        Component.literal("Husk and Stray hands did not show their passive effects"));
    helper.assertFalse(
        owner.hasEffect(MobEffects.HUNGER),
        Component.literal("Husk hand on the shoulder did not remove hunger"));
    helper.assertTrue(
        owner.getTicksFrozen() == 0,
        Component.literal("Stray hand on the shoulder did not stop freezing"));
    helper.succeed();
  }

  public void testShoulderHandsCurePoisonAndGrantNightVision(GameTestHelper helper) {
    ServerPlayer owner = helper.makeMockServerPlayerInLevel();
    sitOnShoulder(helper, owner, BoggedHandEntity.ENTITY_ID);
    sitOnShoulder(helper, owner, WitchHandEntity.ENTITY_ID);
    owner.addEffect(new MobEffectInstance(MobEffects.POISON, OWNER_POISON_TICKS));
    ShoulderPassiveHandler.update(owner);
    helper.assertTrue(
        owner.hasEffect(ShoulderPassive.MOSSY_VEINS.getEffect())
            && owner.hasEffect(ShoulderPassive.WITCH_SIGHT.getEffect()),
        Component.literal("Bogged and Witch hands did not show their passive effects"));
    helper.assertFalse(
        owner.hasEffect(MobEffects.POISON),
        Component.literal("Bogged hand on the shoulder did not remove poison"));
    helper.assertTrue(
        owner.hasEffect(MobEffects.NIGHT_VISION),
        Component.literal("Witch hand on the shoulder did not grant night vision"));
    helper.succeed();
  }

  public void testShoulderHandsHoldOnThroughTheirDamage(GameTestHelper helper) {
    Map<Identifier, Function<DamageSources, DamageSource>> grippingDamage =
        Map.of(
            WitheredHandEntity.ENTITY_ID, DamageSources::onFire,
            StrayHandEntity.ENTITY_ID, DamageSources::freeze,
            BoggedHandEntity.ENTITY_ID, DamageSources::magic,
            DrownedHandEntity.ENTITY_ID, DamageSources::drown,
            HuskHandEntity.ENTITY_ID, DamageSources::starve);
    Map<Identifier, ServerPlayer> owners = new HashMap<>();
    grippingDamage
        .keySet()
        .forEach(handId -> owners.put(handId, sitBesideRottenHand(helper, handId)));
    helper.runAfterDelay(
        AFTER_SHOULDER_GRACE_TICKS,
        () -> {
          grippingDamage.forEach(
              (handId, damage) -> {
                ServerPlayer owner = owners.get(handId);
                DamageSource damageSource = damage.apply(owner.damageSources());
                owner.hurtServer(helper.getLevel(), damageSource, 1.0F);
                assertOnlyGrippingHandStayed(
                    helper, owner, handId, damageSource.getMsgId() + " damage");

                owner.hurtServer(helper.getLevel(), owner.damageSources().generic(), 1.0F);
                helper.assertFalse(
                    isOnShoulder(owner, ShoulderSide.LEFT, handId),
                    Component.literal(handId + " held on through generic damage"));
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
          drownedOwner.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(TEST_POSITION)));
          drownedOwner.doTick();
          helper.assertTrue(
              drownedOwner.isInWater(), Component.literal("The owner did not enter the water"));
          assertOnlyGrippingHandStayed(helper, drownedOwner, DrownedHandEntity.ENTITY_ID, "water");

          // Entity.baseTick resets isInPowderSnow before the shoulder check of the same tick.
          strayOwner.setIsInPowderSnow(true);
          strayOwner.handleShoulderEntities();
          assertOnlyGrippingHandStayed(
              helper, strayOwner, StrayHandEntity.ENTITY_ID, "powder snow");
          helper.succeed();
        });
  }
}
