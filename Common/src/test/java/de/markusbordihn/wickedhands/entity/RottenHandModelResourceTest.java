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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.wickedhands.TestResources;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RottenHandModelResourceTest {

  private static final String PROFILE =
      "data/wicked_hands/easy_model_entities/profiles/entity/rotten_hand.json";
  private static final String RENDER_PROFILE =
      "assets/wicked_hands/easy_model_entities/render_profiles/entity/rotten_hand.json";
  private static final List<String> REQUIRED_ANIMATIONS =
      List.of("idle", "walk", "attack", "attack_2");

  private static JsonObject readModel() {
    String modelId = TestResources.readJson(RENDER_PROFILE).get("model").getAsString();
    return TestResources.readJson(TestResources.assetPath(modelId, "", ".bbmodel"));
  }

  @Test
  @DisplayName("Server profile and render profile describe the same model version")
  void profilesMatch() {
    JsonObject profile = TestResources.readJson(PROFILE);
    JsonObject renderProfile = TestResources.readJson(RENDER_PROFILE);
    assertEquals(profile.get("version"), renderProfile.get("version"));
    assertEquals(profile.get("preset_type"), renderProfile.get("preset_type"));
  }

  @Test
  @DisplayName("Render profile points at an existing model and texture")
  void renderProfileResourcesExist() {
    JsonObject renderProfile = TestResources.readJson(RENDER_PROFILE);
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
