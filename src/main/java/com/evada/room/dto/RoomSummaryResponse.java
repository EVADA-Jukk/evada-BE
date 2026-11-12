package com.evada.room.dto;

/** 방탈출 목록 카드 / 시작 화면 정보 */
public record RoomSummaryResponse(
        Long roomId,
        String title,
        String imageUrl,
        long playCount,
        Long averageClearSeconds
) {
}
