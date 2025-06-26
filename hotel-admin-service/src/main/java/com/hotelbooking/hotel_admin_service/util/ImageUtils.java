package com.hotelbooking.hotel_admin_service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUtils {

    private final S3Client s3Client;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucket}")
    private String bucketName;

    private static final List<String> SUPPORTED_FILE_TYPES = List.of("image/png", "image/jpeg", "image/jpg", "image/webp");


    public ImageUtils(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Saves an image file locally and uploads it to S3 in multiple formats.
     *
     * @param file The uploaded image file.
     * @param salt A salt string to create a unique name.
     * @return The unique name of the uploaded file.
     * @throws IOException If an error occurs while saving the file.
     */
    public String saveImageFile(MultipartFile file, String salt) throws IOException {
        // Validate the file type
        validateFileType(file.getContentType());

        // Generate a unique name for the file
        String uniqueName = createUniqueImageName(salt);

        // Convert the uploaded file to JPEG format
        byte[] jpegBytes = convertToJpeg(file.getBytes());

        // Upload the converted image to S3
        uploadToS3(jpegBytes, uniqueName + ".jpeg");

        // Return the public URL of the uploaded file
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + uniqueName + ".jpeg";
    }

    private byte[] convertToJpeg(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        BufferedImage jpegImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        jpegImage.createGraphics().drawImage(originalImage, 0, 0, java.awt.Color.WHITE, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(jpegImage, "jpeg", outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Validates the uploaded file type.
     *
     * @param contentType The MIME type of the uploaded file.
     */
    private void validateFileType(String contentType) {
        if (contentType == null || !SUPPORTED_FILE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported file type: " + contentType);
        }
    }

    /**
     * Uploads a file to AWS S3.
     *
     * @param fileData The file data to upload.
     * @param fileName The name of the file in the S3 bucket.
     */
    private void uploadToS3(byte[] fileData, String fileName) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(fileData)
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to S3: " + fileName, e);
        }
    }

    public void deleteFile(String imageUrl) {
        try {
            String fileName = extractFileNameFromUrl(imageUrl);

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image from S3", e);
        }
    }

    /**
     * Creates a unique image name by combining a salt and a random UUID.
     *
     * @param salt The salt to use in the name.
     * @return A unique image name.
     */
    public String createUniqueImageName(String salt) {
        return salt + "_" + UUID.randomUUID();
    }

    private String extractFileNameFromUrl(String url) {
        if (url == null || !url.contains("/")) {
            throw new IllegalArgumentException("Invalid image URL");
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }
}