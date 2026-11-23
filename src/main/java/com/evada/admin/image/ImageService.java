package com.evada.admin.image;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
public class ImageService {

    // 허용 Content-Type → 저장 확장자
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;
    private final String region;

    public ImageService(
            S3Client s3Client,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.s3.endpoint:}") String endpoint,
            @Value("${cloud.aws.region.static}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.region = region;
    }

    public ImageUploadResponse upload(MultipartFile file, ImageCategory category) {
        validate(file);

        String ext = ALLOWED_TYPES.get(file.getContentType());
        String key = category.getFolder() + "/" + UUID.randomUUID() + "." + ext;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다.", e);
        }

        return new ImageUploadResponse(key, toUrl(key));
    }

    public String toUrl(String key) {
        if (!endpoint.isBlank()) {
            return endpoint + "/" + bucket + "/" + key;   // LocalStack
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
        }
        if (!ALLOWED_TYPES.containsKey(file.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "jpg, png, webp 형식의 이미지만 업로드할 수 있습니다.");
        }
    }
    
    public void validateImageKey(String key, ImageCategory category) {
        if (!key.startsWith(category.getFolder() + "/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 이미지 key입니다.");
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드되지 않은 이미지입니다.");
        }
    }
    
 // ImageService에 추가 (import java.util.List)
    public List<ImageUploadResponse> uploadAll(List<MultipartFile> files, ImageCategory category) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        files.forEach(this::validate);
        return files.stream().map(file -> upload(file, category)).toList();
    }
    
    
}