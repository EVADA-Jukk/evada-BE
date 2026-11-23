package com.evada.play.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerSubmitRequest(
        @NotBlank @Size(max = 255) String submittedAnswer
) {
}
