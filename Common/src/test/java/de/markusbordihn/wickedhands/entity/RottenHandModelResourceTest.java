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

package de.markusbordihn.wickedhands.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileParser;
import de.markusbordihn.wickedhands.Constants;
import de.markusbordihn.wickedhands.TestResources;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RottenHandModelResourceTest {

  private static final List<String> REQUIRED_ANIMATIONS =
      List.of("idle", "walk", "attack", "attack_2");

  private static Identifier profileId(String entityId) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/" + entityId);
  }

  private static String profile(String entityId) {
    return "data/wicked_hands/easy_model_entities/profiles/entity/" + entityId + ".json";
  }

  private static String renderProfile(String entityId) {
    return "assets/wicked_hands/easy_model_entities/render_profiles/entity/" + entityId + ".json";
  }

  private static JsonObject readModel() {
    return TestResources.readJson(
        TestResources.assetPath(
            TestResources.readJson(renderProfile(RottenHandEntity.ID)).get("model").getAsString(),
            "",
            ".bbmodel"));
  }

  private static EasyModelRenderProfile parseRenderProfile(String entityId) throws IOException {
    try (Reader reader =
        Files.newBufferedReader(TestResources.resourcePath(renderProfile(entityId)))) {
      return ModelRenderProfileParser.parse(profileId(entityId), reader);
    }
  }

  private static ResourceManager assetResourceManager() {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(any()))
        .thenAnswer(invocation -> assetResource(invocation.getArgument(0)));
    return resourceManager;
  }

  private static Optional<Resource> assetResource(Identifier resourceId) {
    Path path = TestResources.assetPath(resourceId.toString(), "", "");
    if (!Files.exists(path)) {
      return Optional.empty();
    }

    return Optional.of(new Resource(mock(PackResources.class), () -> Files.newInputStream(path)));
  }

  @ParameterizedTest
  @ValueSource(strings = {RottenHandEntity.ID, LifelessRottenHandEntity.ID})
  @DisplayName("Easy Model Entities accepts the server profile")
  void serverProfileIsActive(String entityId) throws IOException {
    EasyModelEntityProfile profile;
    try (Reader reader = Files.newBufferedReader(TestResources.resourcePath(profile(entityId)))) {
      profile = EasyModelProfileParser.parse(profileId(entityId), reader);
    }
    assertTrue(profile.isActive(), profile.status() + " " + profile.validationIssues());
  }

  @ParameterizedTest
  @ValueSource(strings = {RottenHandEntity.ID, LifelessRottenHandEntity.ID})
  @DisplayName("Easy Model Entities accepts the render profile")
  void renderProfileIsActive(String entityId) throws IOException {
    EasyModelRenderProfile renderProfile = parseRenderProfile(entityId);
    assertTrue(
        renderProfile.isActive(), renderProfile.status() + " " + renderProfile.validationIssues());
  }

  @ParameterizedTest
  @ValueSource(strings = {RottenHandEntity.ID, LifelessRottenHandEntity.ID})
  @DisplayName("Model bakes for its body type without the fallback model")
  void modelBakesWithoutFallback(String entityId) throws IOException {
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault().bake(parseRenderProfile(entityId), assetResourceManager());
    assertTrue(bakeResult.successful(), bakeResult.validationIssues().toString());
  }

  @ParameterizedTest
  @ValueSource(strings = {RottenHandEntity.ID, LifelessRottenHandEntity.ID})
  @DisplayName("Server profile and render profile describe the same model version")
  void profilesMatch(String entityId) {
    JsonObject profile = TestResources.readJson(profile(entityId));
    JsonObject renderProfile = TestResources.readJson(renderProfile(entityId));
    assertEquals(profile.get("version"), renderProfile.get("version"));
    assertEquals(profile.get("preset_type"), renderProfile.get("preset_type"));
  }

  @Test
  @DisplayName("Lifeless hand uses the rotten hand model without any animation")
  void lifelessHandIsMotionless() throws IOException {
    assertEquals(
        ModelAnimationMode.NONE,
        parseRenderProfile(LifelessRottenHandEntity.ID).animation().mode());
    assertEquals(
        TestResources.readJson(renderProfile(RottenHandEntity.ID)).get("model"),
        TestResources.readJson(renderProfile(LifelessRottenHandEntity.ID)).get("model"));
  }

  @ParameterizedTest
  @ValueSource(strings = {RottenHandEntity.ID, LifelessRottenHandEntity.ID})
  @DisplayName("Render profile points at an existing model and texture")
  void renderProfileResourcesExist(String entityId) {
    JsonObject renderProfile = TestResources.readJson(renderProfile(entityId));
    String modelId = renderProfile.get("model").getAsString();
    String textureId = renderProfile.get("texture").getAsString();
    assertTrue(Files.exists(TestResources.assetPath(modelId, "", ".bbmodel")), modelId);
    assertTrue(Files.exists(TestResources.assetPath(textureId, "", "")), textureId);
  }

  @Test
  @DisplayName("Model has idle, walk and attack animations with a variant")
  void requiredAnimationsExist() {
    Set<String> animationNames =
        readModel().getAsJsonArray("animations").asList().stream()
            .map(animation -> animation.getAsJsonObject().get("name").getAsString())
            .collect(Collectors.toSet());
    assertTrue(
        animationNames.containsAll(REQUIRED_ANIMATIONS),
        "Missing animations, found " + animationNames);
  }

  @Test
  @DisplayName("Every animated bone exists in the model")
  void animatorsReferenceExistingBones() {
    JsonObject model = readModel();
    Set<String> boneIds =
        model.getAsJsonArray("groups").asList().stream()
            .map(group -> group.getAsJsonObject().get("uuid").getAsString())
            .collect(Collectors.toSet());
    for (JsonElement animation : model.getAsJsonArray("animations")) {
      JsonObject animationObject = animation.getAsJsonObject();
      if (!animationObject.has("animators")) {
        continue;
      }

      for (String animatorId : animationObject.getAsJsonObject("animators").keySet()) {
        assertTrue(
            boneIds.contains(animatorId),
            animationObject.get("name").getAsString() + " animates unknown bone " + animatorId);
      }
    }
  }
}
