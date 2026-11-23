package com.evada.play.dto;

import com.evada.play.entity.PlayStatus;

/** 결과 화면. rank는 클리어(COMPLETED)한 경우에만 있다. */
public record PlayResultResponse(
        Long playId,
        Long roomId,
        String roomTitle,
        String nickname,
        PlayStatus status,
        long elapsedSeconds,
        int progress,
        int hintCount,
        int answerRevealCount,
        Long rank,
        long totalClearCount
) {
}
