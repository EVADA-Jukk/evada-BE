package com.evada.play.dto;

import com.evada.play.entity.ProgressStatus;

/** 정답이면 nextContentId로 이동, 오답이면 같은 문제에서 재입력 */
public record AnswerSubmitResponse(
        Long contentId,
        boolean correct,
        ProgressStatus status,
        Long nextContentId,
        int progress,
        boolean allSolved
) {
}
