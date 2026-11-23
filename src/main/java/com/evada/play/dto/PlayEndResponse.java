package com.evada.play.dto;

import com.evada.play.entity.PlayStatus;
import java.time.LocalDateTime;

public record PlayEndResponse(
        Long playId,
        PlayStatus status,
        int progress,
        LocalDateTime endAt,
        long elapsedSeconds
) {
}
