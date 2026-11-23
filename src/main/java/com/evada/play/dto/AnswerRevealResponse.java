package com.evada.play.dto;

/** 정답 보기. 해당 문제는 해결 처리되고 nextContentId로 이동한다. */
public record AnswerRevealResponse(
        Long contentId,
        String answer,
        Long nextContentId,
        int progress,
        int answerRevealCount,
        boolean allSolved
) {
}
