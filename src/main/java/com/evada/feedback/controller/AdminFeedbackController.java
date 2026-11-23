package com.evada.feedback.controller;

import com.evada.common.dto.PageResponse;
import com.evada.feedback.dto.FeedbackResponse;
import com.evada.feedback.entity.FeedbackType;
import com.evada.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 제보함")
@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "제보 목록", description = "type을 빼면 문의·제보·테마 신청 전체. 최신순.")
    @GetMapping
    public PageResponse<FeedbackResponse> getFeedbacks(
            @RequestParam(required = false) FeedbackType type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return feedbackService.getFeedbacks(type, page, size);
    }
}
