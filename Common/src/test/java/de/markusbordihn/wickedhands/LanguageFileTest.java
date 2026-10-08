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

package de.markusbordihn.wickedhands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LanguageFileTest {

  private static final String LANGUAGE_FOLDER = "assets/" + Constants.MOD_ID + "/lang/";
  private static final List<String> REQUIRED_KEYS =
      List.of(
          "entity.wicked_hands.rotten_hand",
          "item.wicked_hands.lifeless_rotten_hand",
          "item.wicked_hands.lifeless_rotten_hand.tooltip",
          "item.wicked_hands.lifeless_rotten_hand.revival_hint");

  private static JsonObject readLanguage(String language) {
    return TestResources.readJson(LANGUAGE_FOLDER + language + ".json");
  }

  @Test
  @DisplayName("German and English translate the same keys")
  void languagesShareKeys() {
    assertEquals(readLanguage("en_us").keySet(), readLanguage("de_de").keySet());
  }

  @ParameterizedTest
  @ValueSource(strings = {"en_us", "de_de"})
  @DisplayName("Every key used by the mod has a non-empty translation")
  void requiredKeysAreTranslated(String language) {
    JsonObject translations = readLanguage(language);
    for (String key : REQUIRED_KEYS) {
      assertTrue(translations.has(key), language + " is missing " + key);
      assertFalse(
          translations.get(key).getAsString().isBlank(), language + " leaves " + key + " empty");
    }
  }
}
