package com.evada.play.dto;

import com.evada.play.entity.PlayStatus;
import java.time.LocalDateTime;

/** 플레이 상태. 재접속 시 currentContentId 화면부터 이어서 진행한다. */
public record PlayResponse(
        Long playId,
        Long roomId,
        String nickname,
        PlayStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        long elapsedSeconds,
        int progress,
        int hintCount,
        int answerRevealCount,
        Long currentContentId
) {
}
