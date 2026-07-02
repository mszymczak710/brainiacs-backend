package com.brainiacs.backend.user;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

@Component
public class ImageResizer {

  private static final int MAX_SIZE = 128;

  public byte[] resize(byte[] originalImage, String contentType) throws IOException {
    String format = extractFormat(contentType);

    BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalImage));
    if (original == null) {
      throw new IOException("Unable to read image: unsupported or corrupted format");
    }

    if (original.getWidth() <= MAX_SIZE && original.getHeight() <= MAX_SIZE) {
      return encodeToBytes(original, format);
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Thumbnails.of(original)
        .size(MAX_SIZE, MAX_SIZE)
        .outputFormat(format)
        .toOutputStream(outputStream);

    return outputStream.toByteArray();
  }

  private byte[] encodeToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, format, outputStream);
    return outputStream.toByteArray();
  }

  private String extractFormat(String contentType) {
    return switch (contentType) {
      case "image/png" -> "png";
      case "image/jpeg", "image/jpg" -> "jpg";
      case "image/webp" -> "webp";
      default -> "jpg";
    };
  }
}
