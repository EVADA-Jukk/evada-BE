package com.evada.admin.dto;

import java.math.BigDecimal;

import com.evada.admin.entity.ContentInputType;
import com.evada.admin.entity.ContentType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentItemRequest(
        @NotBlank String imageKey,
        @NotNull ContentType type,
        @Size(max = 255) String answer,
        ContentInputType inputType,
        @Size(max = 255) String hint,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal nextButtonX,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal nextButtonY,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal nextButtonWidth,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal nextButtonHeight
) {
}