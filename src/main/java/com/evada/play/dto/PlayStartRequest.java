package com.evada.play.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlayStartRequest(
        @NotNull Long roomId,
        @NotBlank @Size(max = 20) String nickname
) {
}
