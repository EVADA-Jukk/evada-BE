package com.evada.play.service;

import com.evada.admin.entity.Content;
import com.evada.admin.entity.ContentType;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.play.entity.ContentProgress;
import java.util.List;
import java.util.Map;

/** 한 플레이의 콘텐츠 순서(sequence 오름차순)와 문제별 진행 기록 */
record PlayState(List<Content> contents, Map<Long, ContentProgress> progressByContentId) {

    Content findProblem(Long contentId) {
        Content content = contents.stream()
                .filter(c -> c.getContentId().equals(contentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND));
        if (content.getType() != ContentType.PROBLEM) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_PROBLEM);
        }
        return content;
    }

    ContentProgress progress(Content content) {
        return progressByContentId.get(content.getContentId());
    }

    boolean isSolved(Content content) {
        ContentProgress progress = progress(content);
        return progress != null && progress.isCompleted();
    }

    /** 앞 순서의 문제를 모두 풀어야 다음 문제를 풀 수 있다. */
    void validateUnlocked(Content content) {
        boolean locked = problems().stream()
                .filter(c -> c.getSequence() < content.getSequence())
                .anyMatch(c -> !isSolved(c));
        if (locked) {
            throw new BusinessException(ErrorCode.CONTENT_LOCKED);
        }
    }

    void put(ContentProgress progress) {
        progressByContentId.put(progress.getContent().getContentId(), progress);
    }

    int problemCount() {
        return problems().size();
    }

    int solvedCount() {
        return (int) problems().stream().filter(this::isSolved).count();
    }

    boolean allSolved() {
        return solvedCount() == problemCount();
    }

    Long nextContentId(Content content) {
        return contents.stream()
                .filter(c -> c.getSequence() > content.getSequence())
                .findFirst()
                .map(Content::getContentId)
                .orElse(null);
    }

    /** 마지막으로 해결한 문제의 다음 화면. 아무것도 못 풀었으면 첫 화면, 다 풀었으면 null. */
    Integer currentSequence() {
        int lastSolved = problems().stream()
                .filter(this::isSolved)
                .mapToInt(Content::getSequence)
                .max()
                .orElse(Integer.MIN_VALUE);
        return contents.stream()
                .map(Content::getSequence)
                .filter(seq -> seq > lastSolved)
                .findFirst()
                .orElse(null);
    }

    /** 재접속 시 이어서 보여줄 화면. 다 풀었으면 마지막 화면. */
    Long currentContentId() {
        if (contents.isEmpty()) {
            return null;
        }
        Integer current = currentSequence();
        if (current == null) {
            return contents.get(contents.size() - 1).getContentId();
        }
        return contents.stream()
                .filter(c -> c.getSequence().equals(current))
                .findFirst()
                .map(Content::getContentId)
                .orElse(null);
    }

    private List<Content> problems() {
        return contents.stream().filter(c -> c.getType() == ContentType.PROBLEM).toList();
    }
}
