package com.evada.feedback.dto;

import com.evada.feedback.entity.Feedback;
import com.evada.feedback.entity.FeedbackType;
import java.time.LocalDateTime;

public record FeedbackResponse(
        Long feedbackId,
        FeedbackType type,
        String content,
        Long roomId,
        Long playId,
        LocalDateTime createdAt
) {
    public static FeedbackResponse from(Feedback f) {
        return new FeedbackResponse(f.getFeedbackId(), f.getType(), f.getContent(),
                f.getRoomId(), f.getPlayId(), f.getCreatedAt());
    }
}
