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
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.wickedhands.gradle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateLifelessTexturesTask extends DefaultTask {

  private static final double KEPT_SATURATION = 0.25;
  private static final double DARKEST_BRIGHTNESS = 0.34;
  private static final double BRIGHTEST_BRIGHTNESS = 0.97;
  private static final double CLIPPED_LUMA_FRACTION = 0.02;
  private static final double[] BONE_WHITE_TINT = {1.0, 0.985, 0.94};
  private static final String ENTITY_TEXTURE_PATH = "entity/%s_hand.png";
  private static final String LIFELESS_TEXTURE_PATH = "item/lifeless_%s_hand.png";

  private static double luma(int pixel) {
    return (0.299 * ((pixel >> 16) & 0xFF) + 0.587 * ((pixel >> 8) & 0xFF) + 0.114 * (pixel & 0xFF))
        / 255.0;
  }

  private static BufferedImage toLifeless(BufferedImage source) {
    int width = source.getWidth();
    int height = source.getHeight();
    int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);
    double[] opaqueLumas =
        Arrays.stream(pixels)
            .filter(pixel -> (pixel >>> 24) > 0)
            .mapToDouble(GenerateLifelessTexturesTask::luma)
            .sorted()
            .toArray();
    if (opaqueLumas.length == 0) {
      return source;
    }

    double darkestLuma = opaqueLumas[(int) (opaqueLumas.length * CLIPPED_LUMA_FRACTION)];
    double brightestLuma = opaqueLumas[(int) (opaqueLumas.length * (1.0 - CLIPPED_LUMA_FRACTION))];
    double lumaSpan = Math.max(brightestLuma - darkestLuma, 1.0 / 255.0);

    for (int i = 0; i < pixels.length; i++) {
      if ((pixels[i] >>> 24) > 0) {
        pixels[i] = toLifelessPixel(pixels[i], darkestLuma, lumaSpan);
      }
    }
    BufferedImage lifeless = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    lifeless.setRGB(0, 0, width, height, pixels, 0, width);
    return lifeless;
  }

  private static int toLifelessPixel(int pixel, double darkestLuma, double lumaSpan) {
    double luma = luma(pixel);
    double normalizedLuma = Math.clamp((luma - darkestLuma) / lumaSpan, 0.0, 1.0);
    int lifelessPixel = pixel & 0xFF000000;
    for (int channel = 0; channel < 3; channel++) {
      int shift = 16 - channel * 8;
      double value = normalizedLuma + (((pixel >> shift) & 0xFF) / 255.0 - luma) * KEPT_SATURATION;
      value = DARKEST_BRIGHTNESS + (BRIGHTEST_BRIGHTNESS - DARKEST_BRIGHTNESS) * value;
      value *= BONE_WHITE_TINT[channel];
      lifelessPixel |= (int) Math.round(Math.clamp(value, 0.0, 1.0) * 255.0) << shift;
    }
    return lifelessPixel;
  }

  @Internal
  public abstract DirectoryProperty getTextureDirectory();

  @Input
  public abstract ListProperty<String> getHandTypes();

  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  public FileCollection getEntityTextures() {
    return this.textureFiles(ENTITY_TEXTURE_PATH);
  }

  @OutputFiles
  public FileCollection getLifelessTextures() {
    return this.textureFiles(LIFELESS_TEXTURE_PATH);
  }

  private FileCollection textureFiles(String pathPattern) {
    return this.getTextureDirectory()
        .files(
            this.getHandTypes()
                .map(handTypes -> handTypes.stream().map(pathPattern::formatted).toList()));
  }

  @TaskAction
  public void generate() throws IOException {
    File textureDirectory = this.getTextureDirectory().get().getAsFile();
    for (String handType : this.getHandTypes().get()) {
      File source = new File(textureDirectory, ENTITY_TEXTURE_PATH.formatted(handType));
      if (!source.isFile()) {
        throw new GradleException("Missing entity texture " + source);
      }

      BufferedImage sourceImage = ImageIO.read(source);
      if (sourceImage == null) {
        throw new GradleException("Unreadable entity texture " + source);
      }

      File target = new File(textureDirectory, LIFELESS_TEXTURE_PATH.formatted(handType));
      ImageIO.write(toLifeless(sourceImage), "png", target);
      this.getLogger().lifecycle("Generated {}", target.getName());
    }
  }
}
