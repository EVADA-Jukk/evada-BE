package com.evada.play.dto;

public record HintResponse(
        Long contentId,
        String hint,
        int hintCount
) {
}
