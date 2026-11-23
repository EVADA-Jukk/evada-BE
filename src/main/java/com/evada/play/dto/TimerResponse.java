package com.evada.play.dto;

import java.time.LocalDateTime;

/** 제한시간 없음. 서버 시간 기준 경과 시간만 내려준다. */
public record TimerResponse(
        LocalDateTime startAt,
        LocalDateTime endAt,
        long elapsedSeconds
) {
}
