package com.evada.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evada.admin.dto.ContentCreateRequest;
import com.evada.admin.dto.ContentCreateResponse;
import com.evada.admin.service.ContentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 콘텐츠")
@RestController
@RequestMapping("/api/admin/rooms/{roomId}/contents")
@RequiredArgsConstructor
public class AdminContentController {

    private final ContentService contentService;

    @Operation(summary = "콘텐츠 일괄 등록", description = "목록 순서대로 sequence가 매겨집니다.")
    @PostMapping
    public ResponseEntity<ContentCreateResponse> createContents(
            @PathVariable Long roomId,
            @Valid @RequestBody ContentCreateRequest request) {
        return ResponseEntity.ok(contentService.createContents(roomId, request));
    }
}