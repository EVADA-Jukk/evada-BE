package com.evada.play.dto;

public record ProgressResponse(
        Long playId,
        long solvedCount,
        long totalProblemCount,
        int progress
) {
}
