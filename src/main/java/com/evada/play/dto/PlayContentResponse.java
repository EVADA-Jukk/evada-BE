package com.evada.play.dto;

import com.evada.admin.entity.ContentType;

public record PlayContentResponse(
        Long contentId,
        int sequence,
        ContentType type,
        PlayContentStatus status,
        boolean hintUsed,
        boolean answerRevealed
) {
}
