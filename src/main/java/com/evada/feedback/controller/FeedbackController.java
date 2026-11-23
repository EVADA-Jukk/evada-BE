package com.evada.feedback.controller;

import com.evada.feedback.dto.FeedbackCreateRequest;
import com.evada.feedback.dto.FeedbackCreateResponse;
import com.evada.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "제보함")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "제보 보내기", description = "type: INQUIRY(문의) / REPORT(오류 제보) / THEME_REQUEST(테마 신청). 플레이 중이면 playId를 함께 보냅니다.")
    @PostMapping
    public ResponseEntity<FeedbackCreateResponse> create(@Valid @RequestBody FeedbackCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.create(request));
    }
}
