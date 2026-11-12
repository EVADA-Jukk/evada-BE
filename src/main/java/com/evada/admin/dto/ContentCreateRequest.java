package com.evada.admin.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ContentCreateRequest(
        @NotEmpty @Valid List<ContentItemRequest> contents
) {
}