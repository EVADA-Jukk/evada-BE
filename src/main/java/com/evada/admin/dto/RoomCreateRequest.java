package com.evada.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomCreateRequest(
		@NotBlank String title,
		@NotBlank String imageKey
		) {

}
