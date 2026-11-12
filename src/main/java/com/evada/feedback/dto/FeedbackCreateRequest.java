package com.evada.feedback.dto;

import com.evada.feedback.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** roomId, playId는 게임 중에 제보할 때만 보낸다. */
public record FeedbackCreateRequest(
        @NotNull FeedbackType type,
        @NotBlank @Size(max = 1000) String content,
        Long roomId,
        Long playId
) {
}
