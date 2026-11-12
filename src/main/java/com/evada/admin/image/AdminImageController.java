package com.evada.admin.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "관리자 이미지")
@RestController
@RequestMapping("/api/admin/images")
public class AdminImageController {

    private final ImageService imageService;

    public AdminImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "이미지 업로드",
            description = "이미지를 S3에 업로드하고 key와 url을 반환합니다. (jpg, png, webp)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("category") ImageCategory category) {
        return ResponseEntity.ok(imageService.upload(file, category));
    }
    
 // import java.util.List 추가
    @Operation(summary = "이미지 일괄 업로드",
            description = "여러 장을 올리고 파일 순서대로 key와 url을 반환합니다.")
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageUploadResponse>> uploadAll(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("category") ImageCategory category) {
        return ResponseEntity.ok(imageService.uploadAll(files, category));
    }
}