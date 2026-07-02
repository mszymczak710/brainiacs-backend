package com.brainiacs.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainiacs.backend.user.ImageResizer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageResizerTest {

  private final ImageResizer imageResizer = new ImageResizer();

  @Test
  void resize_shouldShrinkLargeImageTo128Max() throws Exception {
    byte[] largeImage = createTestImage(500, 300, "png");

    byte[] result = imageResizer.resize(largeImage, "image/png");

    BufferedImage resized = ImageIO.read(new ByteArrayInputStream(result));
    assertThat(resized.getWidth()).isLessThanOrEqualTo(128);
    assertThat(resized.getHeight()).isLessThanOrEqualTo(128);
  }

  @Test
  void resize_shouldPreserveAspectRatio() throws Exception {
    byte[] wideImage = createTestImage(400, 100, "png"); // 4:1

    byte[] result = imageResizer.resize(wideImage, "image/png");

    BufferedImage resized = ImageIO.read(new ByteArrayInputStream(result));
    double ratio = (double) resized.getWidth() / resized.getHeight();
    assertThat(ratio).isCloseTo(4.0, org.assertj.core.data.Offset.offset(0.1));
  }

  @Test
  void resize_shouldNotUpscaleSmallImage() throws Exception {
    byte[] smallImage = createTestImage(50, 50, "png");

    byte[] result = imageResizer.resize(smallImage, "image/png");

    BufferedImage resized = ImageIO.read(new ByteArrayInputStream(result));
    assertThat(resized.getWidth()).isEqualTo(50);
    assertThat(resized.getHeight()).isEqualTo(50);
  }

  @Test
  void resize_shouldThrow_whenImageIsCorrupted() {
    byte[] garbage = "not-an-image".getBytes();

    assertThatThrownBy(() -> imageResizer.resize(garbage, "image/png"))
        .isInstanceOf(java.io.IOException.class);
  }

  private byte[] createTestImage(int width, int height, String format) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, format, out);
    return out.toByteArray();
  }
}
