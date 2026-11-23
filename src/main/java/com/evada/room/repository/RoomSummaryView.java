package com.evada.room.repository;

/** 방 목록/상세 카드용 집계 결과 */
public interface RoomSummaryView {

    Long getRoomId();

    String getTitle();

    String getImageKey();

    Long getPlayCount();

    /** COMPLETED 플레이 평균 경과 시간(초). 클리어 기록이 없으면 null */
    Double getAverageClearSeconds();
}
