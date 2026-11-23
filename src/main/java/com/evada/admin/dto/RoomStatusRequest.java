package com.evada.admin.dto;

import com.evada.admin.entity.Room;
import jakarta.validation.constraints.NotNull;

public record RoomStatusRequest(@NotNull Room.Status status) {
}
