package com.evada.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),

    // 방
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."),
    ROOM_PRIVATE(HttpStatus.CONFLICT, "공개되지 않은 방입니다."),

    // 콘텐츠
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 콘텐츠입니다."),
    CONTENT_NOT_PROBLEM(HttpStatus.CONFLICT, "문제 콘텐츠가 아닙니다."),
    CONTENT_LOCKED(HttpStatus.CONFLICT, "이전 문제를 먼저 해결해야 합니다."),

    // 플레이
    PLAY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 플레이입니다."),
    PLAY_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 플레이입니다."),
    ALREADY_SOLVED(HttpStatus.CONFLICT, "이미 해결한 문제입니다."),
    NOT_ALL_PROBLEMS_SOLVED(HttpStatus.CONFLICT, "아직 해결하지 않은 문제가 있습니다."),
    RESULT_NOT_AVAILABLE(HttpStatus.CONFLICT, "플레이가 끝난 뒤에 결과를 볼 수 있습니다."),

    // 힌트 / 정답 보기
    HINT_NOT_AVAILABLE(HttpStatus.CONFLICT, "이 문제에는 힌트가 없습니다."),
    HINT_NOT_USED(HttpStatus.CONFLICT, "힌트를 먼저 확인해야 정답을 볼 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
